package com.orama.endpoint.internal

import com.orama.model.answer.*
import com.orama.model.answer.EventType.*
import com.orama.serialize.KSerializerDataChunk
import io.ktor.sse.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

class SSESerializer<T>(
    val sourcesSerializer: KSerializer<T>
) {
    private val state: EventResult<T> = EventResult()

    fun process(event: ServerSentEvent): SSEEvent<T> { // Would recieve a KSerializer
        val kSerializerDataChunk = KSerializerDataChunk(sourcesSerializer)

         val json = Json {
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                contextual(kSerializerDataChunk)
            }
        }

        val data = event.data?.let {
            json.decodeFromString(kSerializerDataChunk, it)
        }

        when(data?.type) {
            TEXT -> state.message += (data.message as StringMessage).content
            SOURCES -> {
                @Suppress("UNCHECKED_CAST")
                state.sources = (data.message as SourcesList<T>).content
            }
            QUERY_TRANSLATED -> state.queryTranslated = (data.message as TranslatedQuery).content
            else -> throw IllegalArgumentException("Unknown data type: ${data?.type}")
        }

        return data
    }

    fun getState(): EventResult<T> {
        return state
    }
}