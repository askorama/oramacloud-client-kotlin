package com.orama.listeners

import com.orama.model.answer.Interaction
import com.orama.model.search.Hit
import kotlinx.serialization.json.JsonElement

interface AnswerEventListener<T> {
    fun onStateChange(interactions: MutableList<Interaction<T>>) {}
    fun onMessageChange(data: String) {}
    fun onMessageLoading(loading: Boolean) {}
    fun onAnswerAborted(aborted: Boolean) {}
    fun onSourceChanged(sources: List<Hit<T>>) {}
    fun onQueryTranslated(query: Map<String, JsonElement>) {}
}