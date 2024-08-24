package com.orama.model.answer

import com.orama.model.search.Hit
import kotlinx.serialization.json.JsonElement

class EventResult<T> (
    var message: String = "",
    var sources: List<Hit<T>> = emptyList(),
    var queryTranslated: Map<String, JsonElement>
)