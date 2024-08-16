package com.orama.model.answer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = EventType.EventTypeSerializer::class)
enum class EventType (
    val value: String
) {
    SOURCES("sources"),
    QUERY_TRANSLATED("query-translated"),
    TEXT("text");

    companion object {
        fun fromValue(value: String): EventType {
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
        }
    }

    object EventTypeSerializer : KSerializer<EventType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EventType", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: EventType) {
            encoder.encodeString(value.value)
        }

        override fun deserialize(decoder: Decoder): EventType {
            return EventType.fromValue(decoder.decodeString())
        }
    }
}

