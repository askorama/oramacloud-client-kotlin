package com.orama.model.search

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SearchResponse(
    val count: Int,
    val elapsed: Elapsed,
    val hits: List<Hit>,
    val facets: Map<String, JsonElement> = emptyMap()
)



