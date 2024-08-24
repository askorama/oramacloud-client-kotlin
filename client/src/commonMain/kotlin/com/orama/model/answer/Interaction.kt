package com.orama.model.answer

import com.orama.model.search.Hit
import kotlinx.serialization.json.JsonElement

data class Interaction<T> (
    val interactionId: String,
    val query: String,
    val response: String,
    val relatedQueries: List<String>?,
    val sources: List<Hit<T>>,
    val translatedQuery: Map<String, JsonElement>,
    val aborted: Boolean = false,
    val loading: Boolean
)