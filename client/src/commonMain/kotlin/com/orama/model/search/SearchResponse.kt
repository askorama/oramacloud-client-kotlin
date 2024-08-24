package com.orama.model.search

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SearchResponse<T>(
    val count: Int,
    val elapsed: Elapsed,
    val hits: List<Hit<T>>,
    val facets: Map<String, JsonElement> = emptyMap()
)
