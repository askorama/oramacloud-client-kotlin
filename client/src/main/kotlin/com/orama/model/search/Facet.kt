package com.orama.model.search

import com.orama.serialize.KSerializerFacet
import kotlinx.serialization.Serializable

@Serializable(with = KSerializerFacet::class)
sealed class Facet {
    @Serializable
    data class StringFacet(val limit: Int, val order: Order) : Facet()

    @Serializable
    data class NumberFacet(val ranges: List<NumberRange>) : Facet()

    @Serializable
    data class BooleanFacet(val isTrue: Boolean, val isFalse: Boolean) : Facet()

    @Serializable
    data class NumberRange(val from: Int, val to: Int)
}