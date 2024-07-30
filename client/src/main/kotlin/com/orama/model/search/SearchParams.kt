package com.orama.model.search

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
class SearchParams private constructor(
    val term: String,
    val mode: Mode,
    val limit: Int?,
    val offset: Int?,
    val returning: List<String>?,
    @Serializable(with = ConditionListSerializer::class)
    val where: List<Condition>?,
    val facets: Map<String, Facet>?
) {
    companion object {
        fun builder(term: String, mode: Mode = Mode.FULLTEXT) = Builder(term, mode)
    }

    enum class Mode {
        @SerialName("fulltext") FULLTEXT,
        @SerialName("vector") VECTOR,
        @SerialName("hybrid") HYBRID
    }

    class Builder(private val term: String, private val mode: Mode) {
        private var limit: Int = 10
        private var offset: Int = 0
        private var returning: List<String>? = null
        private var where: MutableList<Condition>? = null
        private var facets: MutableMap<String, Facet>? = null

        fun limit(limit: Int) = apply { this.limit = limit }
        fun offset(offset: Int) = apply { this.offset = offset }
        fun returning(returning: List<String>) = apply { this.returning = returning }
        fun where(conditions: List<Condition>) = apply {
            if (this.where == null) {
                this.where = mutableListOf()
            }
            this.where!!.addAll(conditions)
        }

        fun facet(key: String, facet: Facet) = apply {
            if(this.facets == null) {
                this.facets = mutableMapOf()
            }
            this.facets!![key] = facet
        }

        fun build() = SearchParams(term, mode, limit, offset, returning, where, facets)
    }

    @Serializable
    data class Facet(
        val limit: Int? = null,
        val order: String? = null,
        val ranges: List<Range>? = null,
        val trueFalse: TrueFalse? = null
    ) {

        @Serializable
        data class Range(val from: Int?, val to: Int?)

        @Serializable
        data class TrueFalse(val trueValue: Boolean, val falseValue: Boolean?)
    }

    @Serializable
    data class Condition(val field: String, val condition: ConditionType)

    // TODO: add geo search
    @Serializable
    sealed class ConditionType {
        data class GreaterThan(val value: Double) : ConditionType()
        data class GreaterThanOrEqual(val value: Double) : ConditionType()
        data class LessThan(val value: Double) : ConditionType()
        data class LessThanOrEqual(val value: Double) : ConditionType()
        data class Between(val value: List<Double>) : ConditionType()

        data class Equals(val value: String) : ConditionType()
        data class In(val value: List<String>) : ConditionType()
        data class Nin(val value: List<String>) : ConditionType()
        data class ContainsAll(val value: List<String>) : ConditionType()
    }

    object ConditionListSerializer : KSerializer<List<Condition>> {
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

    fun toJson(): String {
        return Json.encodeToString(this)
    }
}