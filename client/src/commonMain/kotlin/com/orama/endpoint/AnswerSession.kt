package com.orama.endpoint

import com.orama.endpoint.internal.SSESerializer
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class AnswerSession<T>(
    private val answerParams: AnswerParams<T>,
    private val events: AnswerEventListener<T>? = null,
    private val client: HttpClient = createHttpClient(),
    internal var abortHandler: AbortHandler? = null,
    private val conversationId: String = UUIDUtils.generate()
) : Closeable {
    private val state: MutableList<Interaction<T>> = mutableListOf()

    init {
        abortHandler?.setInterruptCallback {
            events?.onAnswerAborted(true)
            client.close()
        }
    }

    fun getState(): MutableList<Interaction<T>> = this.state
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

    private fun handleAbort() {
        events?.onAnswerAborted(true)
        close()
    }

    private fun handleServerSentEvent(event: ServerSentEvent, sseSerializer: SSESerializer<T>): SSEEvent<T> {

        val chunkData = sseSerializer.process(event)
        val currentState = sseSerializer.getState()

        chunkData.let {
            when (chunkData.type) {
                EventType.SOURCES -> handleSources(currentState.sources)
                EventType.QUERY_TRANSLATED -> handleQueryTranslated(currentState.queryTranslated)
                else -> handleMessageContent(currentState.message)
            }
        }

        return chunkData
    }

    private fun handleSources(sourcesData: List<Hit<T>>) {
        events?.onSourceChanged(sourcesData)
    }

    private fun handleQueryTranslated(queryTranslated: Map<String, JsonElement>) {
        events?.onQueryTranslated(queryTranslated)
    }

    private fun handleMessageContent(messageContent: String) {
        events?.onMessageChange(messageContent)
    }

    private fun <T> emptyEventResult(): EventResult<T> {
        return EventResult(
            message = "",
            sources = emptyList(),
            queryTranslated = emptyMap()
        )
    }

    private suspend fun processAnswer(params: AskParams, onSSEEvent: ((event: String) -> Unit)? = null): String {
        var eventResult = emptyEventResult<T>()

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

                val serializerClass = SSESerializer(sourcesSerializer = answerParams.serializer)


                incoming.collect { item ->
                    val sseEvent = handleServerSentEvent(item, serializerClass)

                    if(sseEvent.type == EventType.TEXT) {
                        onSSEEvent?.invoke((sseEvent.message as StringMessage).content)
                    }
                }

                eventResult = serializerClass.getState()

                updateState(params.query, eventResult.message,  eventResult.sources, eventResult.queryTranslated)
                events?.onStateChange(state)
            }
        } finally {
            events?.onMessageLoading(false)

            return eventResult.message
        }
    }

    suspend fun ask(params: AskParams) : String {
        return this.processAnswer(params)
    }

    suspend fun askStream(params: AskParams, coroutineScope: CoroutineScope): Flow<String> = callbackFlow {
        this@AnswerSession.processAnswer(params) { sseEvent: String ->
            coroutineScope.launch {
                send(sseEvent)
            }
        }

        close()
        awaitClose {}
    }


    private fun buildRequestBody(question: String, messages: List<Message>?, searchParams: Map<String, String>): String {

        var encodedMessages = "[]"
        messages?.let {
            encodedMessages = Json.encodeToString(ListSerializer(Message.serializer()), it)
        }

        val encodedSearchParams = Json.encodeToString(MapSerializer(String.serializer(), String.serializer()), searchParams)

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

    fun clearSession() {
        this.state.clear()
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