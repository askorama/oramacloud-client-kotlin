package com.orama.listeners

import com.orama.model.answer.Interaction
import com.orama.model.search.Hit
import kotlinx.serialization.json.JsonElement

interface AnswerEventListener {
    fun onStateChange(interactions: MutableList<Interaction>) {}
    fun onMessageChange(data: String) {}
    fun onMessageLoading(loading: Boolean) {}
    fun onAnswerAborted(aborted: Boolean) {}
    fun onSourceChanged(sources: List<Hit>) {}
    fun onQueryTranslated(query: Map<String, JsonElement>) {}
}