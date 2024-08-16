package com.orama.serialize

import com.orama.model.search.Condition
import com.orama.model.search.ConditionType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object KSerializerCondition : KSerializer<List<Condition>> {
    override val descriptor: SerialDescriptor =
        ListSerializer(Condition.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: List<Condition>) {
        val jsonEncoder = encoder as JsonEncoder
        val jsonObject = JsonObject(value.groupBy { it.field }.mapValues { entry ->
            JsonObject(entry.value.associate { condition ->
                when (val cond = condition.condition) {
                    is ConditionType.GreaterThan -> "gt" to jsonEncoder.json.encodeToJsonElement(cond.value)
                    is ConditionType.LessThan -> "lt" to jsonEncoder.json.encodeToJsonElement(cond.value)
                    is ConditionType.Equals -> "eq" to jsonEncoder.json.encodeToJsonElement(cond.value)
                    is ConditionType.LessThanOrEqual -> "lte" to jsonEncoder.json.encodeToJsonElement(cond.value)
                    is ConditionType.GreaterThanOrEqual -> "gte" to jsonEncoder.json.encodeToJsonElement(cond.value)
                    is ConditionType.Between -> "between" to JsonArray(cond.value.map { JsonPrimitive(it) })
                    is ConditionType.ContainsAll -> "containsAll" to jsonEncoder.json.encodeToJsonElement(cond.value)
                    is ConditionType.In -> "in" to jsonEncoder.json.encodeToJsonElement(cond.value)
                    is ConditionType.Nin -> "nin" to jsonEncoder.json.encodeToJsonElement(cond.value)
                }
            })
        })
        jsonEncoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): List<Condition> {
        throw UnsupportedOperationException("Deserialization is not supported")
    }
}