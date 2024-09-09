package com.orama.model.search

import com.orama.serialize.KSerializerCondition
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
class SearchParams private constructor (
    val term: String,
    val mode: Mode? = Mode.FULLTEXT,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val returning: List<String>? = null,
    @Serializable(with = KSerializerCondition::class)
    val where: List<Condition>? = null,
    val properties: List<String>? = null,
    val sortBy: Map<String, String>? = null,
    val facets: Map<String, Facet>? = null
) {
    companion object {
        fun builder(term: String, mode: Mode = Mode.FULLTEXT) = Builder(term, mode)
    }

    class Builder(private val term: String, private val mode: Mode) {
        private var limit: Int = 10
        private var offset: Int = 0
        private var returning: List<String>? = null
        private var where: MutableList<Condition>? = null
        private var facets: Map<String, Facet>? = null
        private var sortBy: Map<String, String>? = null
        private var properties: List<String>? = null

        fun properties(properties: List<String>?) = apply { this.properties = properties }
        fun sortBy(sortBy: Map<String, String>) = apply { this. sortBy= sortBy }
        fun facets(facets: Map<String, Facet>) = apply { this.facets = facets }
        fun limit(limit: Int) = apply { this.limit = limit }
        fun offset(offset: Int) = apply { this.offset = offset }
        fun returning(returning: List<String>) = apply { this.returning = returning }
        fun where(conditions: List<Condition>) = apply {
            if (this.where == null) {
                this.where = mutableListOf()
            }
            this.where!!.addAll(conditions)
        }

        fun build() = SearchParams(term, mode, limit, offset, returning, where, properties, sortBy, facets)
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }
}