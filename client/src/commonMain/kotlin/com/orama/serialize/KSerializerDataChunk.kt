package com.orama.serialize

import com.orama.model.answer.*
import com.orama.model.answer.EventType.*
import com.orama.model.search.Hit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.JsonElement


class KSerializerDataChunk<T>(private val tSerializer: KSerializer<T>) : KSerializer<SSEEvent<T>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("IncomingSSEEvent", tSerializer.descriptor) {
        element(elementName =  "type", PrimitiveSerialDescriptor("type", PrimitiveKind.STRING))
        element(elementName =  "interactionId", PrimitiveSerialDescriptor("interactionId", PrimitiveKind.STRING))
        element(elementName =  "message", buildClassSerialDescriptor("MessageContent"))
    }

    override fun deserialize(decoder: Decoder): SSEEvent<T> {
        return decoder.decodeStructure(descriptor) {
            var type: EventType? = null
            var interactionId: String? = null
            var message: MessageContent? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> type = decodeSerializableElement(descriptor, index, EventType.serializer())
                    1 -> interactionId = decodeStringElement(descriptor, index)
                    2 -> message = when (type) {
                        TEXT -> StringMessage(decodeStringElement(descriptor, index))
                        QUERY_TRANSLATED  -> TranslatedQuery(decodeSerializableElement(descriptor, index, MapSerializer(String.serializer(), JsonElement.serializer())))
                        SOURCES -> {
                            val hitSerializer = Hit.serializer(tSerializer)
                            SourcesList(decodeSerializableElement(descriptor, index, ListSerializer(hitSerializer)))
                        }
                        else -> throw SerializationException("Unknown type: ${type}")
                    }
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }

            SSEEvent(
                type = type,
                interactionId = interactionId,
                message = message
            )
        }
    }

    override fun serialize(encoder: Encoder, value: SSEEvent<T>) {
        throw NotImplementedError("Serializer not available.")
    }
}