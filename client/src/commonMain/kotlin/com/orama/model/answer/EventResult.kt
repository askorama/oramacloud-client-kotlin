package com.orama.model.answer

import com.orama.model.search.Hit
import kotlinx.serialization.json.JsonElement

class EventResult (
    var message: String = "",
    var sources: List<Hit> = emptyList(),
    var queryTranslated: Map<String, JsonElement>
)