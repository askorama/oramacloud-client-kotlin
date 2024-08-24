package com.orama.endpoint

import com.orama.listeners.AnswerEventListener
import com.orama.listeners.AbortHandler
import com.orama.model.answer.*
import com.orama.model.search.Hit
import com.orama.utils.UUIDUtils
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.sse.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

class AnswerSession<T>(
    private val answerParams: AnswerParams<T>,
    private val events: AnswerEventListener<T>? = null,
    private val client: HttpClient = createHttpClient(),
    internal var abortHandler: AbortHandler? = null,
    private val conversationId: String = UUIDUtils.generate()
) : Closeable {
    private val state: MutableList<Interaction<T>> = mutableListOf()
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(DataChunkSerializer)
        }
    }

    init {
        abortHandler?.setInterruptCallback {
            events?.onAnswerAborted(true)
            client.close()
        }
    }

    private fun handleAbort() {
        events?.onAnswerAborted(true)
        close()
    }

    private fun handleServerSentEvent(event: ServerSentEvent, eventResult: EventResult<T>): EventResult<T> {
        val chunkData = event.data?.let {
            json.decodeFromString(DataChunkSerializer, it)
        }

        chunkData?.let {
            when (chunkData.type) {
                EventType.SOURCES -> handleSources(chunkData.message as SourcesList<T>) {
                    eventResult.sources = it
                }
                EventType.QUERY_TRANSLATED -> handleQueryTranslated(chunkData.message as TranslatedQuery) {
                    eventResult.queryTranslated = it
                }
                else -> handleMessageContent(chunkData.message as StringMessage) {
                    eventResult.message += it
                }
            }
        }

        return eventResult
    }

    private fun handleSources(sourcesData: SourcesList<T>, onFinished: (List<Hit<T>>) -> Unit) {
        events?.onSourceChanged(sourcesData.content)
        onFinished(sourcesData.content)
    }

    private fun handleQueryTranslated(queryTranslated: TranslatedQuery, onFinished: (Map<String, JsonElement>) -> Unit) {
        events?.onQueryTranslated(queryTranslated.content)
        onFinished(queryTranslated.content)
    }

    private fun handleMessageContent(messageContent: StringMessage, onFinished: (String) -> Unit) {
        events?.onMessageChange(messageContent.content)
        onFinished(messageContent.content)
    }

    private fun <T> emptyEventResult(): EventResult<T> {
        return EventResult(
            message = "",
            sources = emptyList(),
            queryTranslated = emptyMap()
        )
    }

    suspend fun ask(params: AskParams)  {
        try {
            events?.onMessageLoading(true)

            client.sse({
                url {
                    method = HttpMethod.Post
                    protocol = URLProtocol.HTTPS
                    host = "answer.api.orama.com"
                    encodedPath = "/v1/answer"
                    parameters.append("api-key", answerParams.oramaClient.apiKey)
                }

                val body = buildRequestBody(params.query, params.messages, params.searchParams)
                setBody(body)
                contentType(ContentType.Application.FormUrlEncoded)
            }) {
                var eventResult = emptyEventResult<T>()

                incoming.collect { item ->
                    eventResult = handleServerSentEvent(item, eventResult)
                }

                updateState(params.query, eventResult.message,  eventResult.sources, eventResult.queryTranslated)
                events?.onStateChange(state)
            }
        } finally {
            events?.onMessageLoading(false)
        }
    }

    private fun buildRequestBody(question: String, messages: List<Message>?, searchParams: Map<String, String>): String {

        var encodedMessages = "[]"
        messages?.let {
            encodedMessages = json.encodeToString(ListSerializer(Message.serializer()), it)
        }

        val encodedSearchParams = json.encodeToString(MapSerializer(String.serializer(), String.serializer()), searchParams)

        println(encodedMessages)

        return parameters {
            append("query", question)
            append("conversationId", conversationId)
            append("messages", encodedMessages)
            append("userId", UUIDUtils.generate())
            append("endpoint", answerParams.oramaClient.endpoint)
            append("searchParams", encodedSearchParams)
        }.formUrlEncode()
    }

    private fun updateState(
        question: String,
        message: String,
        sources: List<Hit<T>>,
        translatedQuery: Map<String, JsonElement>
    ) {
        val interaction = Interaction(
            interactionId = UUIDUtils.generate(),
            query = question,
            response = message,
            relatedQueries = null,
            sources = sources,
            translatedQuery = translatedQuery,
            aborted = false,
            loading = false
        )

        state.add(interaction)
    }

    private fun getLatestQuery(messages: List<Message>): String {
        if (messages.size < 2) {
            throw IllegalArgumentException("Insufficient previous messages to determine the latest query.")
        }

        val latest = messages[messages.size - 2]

        return latest.content
    }

    suspend fun regenerateLast() {
        val messages = getMessages()
        val latestQuery = getLatestQuery(messages)

        ask(
            AskParams(
                query = latestQuery,
                messages = messages.dropLast(1)
            )
        )
    }

    fun getMessages(): List<Message> {
        val messages = mutableListOf<Message>()

        for (interaction in state) {
            messages.add(Message(
                role = Role.USER,
                content = interaction.query
            ))

            messages.add(Message(
                role = Role.ASSISTANT,
                content = interaction.response
            ))
        }

        return messages
    }

    override fun close() {
        client.close()
    }

    companion object {
        private fun createHttpClient() = HttpClient(CIO) {
            install(SSE)
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }
}