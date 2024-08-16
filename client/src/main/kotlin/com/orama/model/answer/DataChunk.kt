package com.orama.model.answer

import com.orama.model.search.Hit
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
sealed class MessageContent

@Serializable
@SerialName("StringMessage")
data class StringMessage(val content: String) : MessageContent()

@Serializable
@SerialName("TranslatedQueryMap")
data class TranslatedQuery(val content: Map<String, JsonElement>) : MessageContent()

@Serializable
@SerialName("SourcesList")
data class SourcesList(val content: List<Hit>) : MessageContent()

@Serializable
data class DataChunk(
    val type: EventType,
    val message: MessageContent,
    val interactionId: String
)

object DataChunkSerializer : KSerializer<DataChunk> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DataChunk") {
        element("type", EventType.serializer().descriptor)
        element("message", buildClassSerialDescriptor("MessageContent"))
        element("interactionId", PrimitiveSerialDescriptor("interactionId", PrimitiveKind.STRING))
    }

    override fun serialize(encoder: Encoder, value: DataChunk) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, EventType.serializer(), value.type)
            when (val message = value.message) {
                is StringMessage -> encodeStringElement(descriptor, 1, message.content)
                is TranslatedQuery -> encodeSerializableElement(descriptor, 1, MapSerializer(String.serializer(), JsonElement.serializer()), message.content)
                is SourcesList -> encodeSerializableElement(descriptor, 1, ListSerializer(Hit.serializer()), message.content)
            }
            encodeStringElement(descriptor, 2, value.interactionId)
        }
    }

    override fun deserialize(decoder: Decoder): DataChunk {
        return decoder.decodeStructure(descriptor) {
            var type: EventType? = null
            var message: MessageContent? = null
            var interactionId: String? = null

            loop@ while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> type = decodeSerializableElement(descriptor, index, EventType.serializer())
                    1 -> message = when (type) {
                        EventType.TEXT -> StringMessage(decodeStringElement(descriptor, index))
                        EventType.QUERY_TRANSLATED -> TranslatedQuery(decodeSerializableElement(descriptor, index, MapSerializer(String.serializer(), JsonElement.serializer())))
                        EventType.SOURCES -> SourcesList(decodeSerializableElement(descriptor, index, ListSerializer(Hit.serializer())))
                        null -> throw SerializationException("Unknown type")
                    }
                    2 -> interactionId = decodeStringElement(descriptor, index)
                    else -> throw SerializationException("Unknown index $index")
                }
            }

            DataChunk(
                type = type ?: throw SerializationException("Missing type"),
                message = message ?: throw SerializationException("Missing message"),
                interactionId = interactionId ?: throw SerializationException("Missing interactionId")
            )
        }
    }
}