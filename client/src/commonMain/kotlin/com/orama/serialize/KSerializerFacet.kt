package com.orama.serialize

import com.orama.model.search.Facet
import com.orama.model.search.Facet.*
import com.orama.model.search.Order
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object KSerializerFacet : KSerializer<Facet> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Fact", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Facet) {
        val jsonEncoder = encoder as? JsonEncoder ?: error("This class can be serialized only by JSON")

        val jsonObject = when (value) {
            is StringFacet -> JsonObject(
                mapOf(
                    "limit" to Json.encodeToJsonElement(value.limit),
                    "order" to Json.encodeToJsonElement(value.order)
                )
            )
            is NumberFacet -> JsonObject(
                mapOf(
                    "ranges" to Json.encodeToJsonElement(value.ranges)
                )
            )
            is BooleanFacet -> JsonObject(
                mapOf(
                    "isTrue" to Json.encodeToJsonElement(value.isTrue),
                    "isFalse" to Json.encodeToJsonElement(value.isFalse)
                )
            )
        }

        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): Facet {
        val jsonDecoder = decoder as? JsonDecoder ?: error("This class can be deserialized only by JSON")
        val jsonElement = jsonDecoder.decodeJsonElement().jsonObject

        return when {
            "limit" in jsonElement && "order" in jsonElement -> {
                val limit = jsonElement["limit"]!!.jsonPrimitive.int
                val order = Json.decodeFromJsonElement<Order>(jsonElement["order"]!!)
                StringFacet(limit, order)
            }
            "ranges" in jsonElement -> {
                val ranges = Json.decodeFromJsonElement<List<NumberRange>>(jsonElement["ranges"]!!)
                NumberFacet(ranges)
            }
            "isTrue" in jsonElement && "isFalse" in jsonElement -> {
                val isTrue = jsonElement["isTrue"]!!.jsonPrimitive.boolean
                val isFalse = jsonElement["isFalse"]!!.jsonPrimitive.boolean
                BooleanFacet(isTrue, isFalse)
            }
            else -> throw SerializationException("Unknown Facet type")
        }
    }
}