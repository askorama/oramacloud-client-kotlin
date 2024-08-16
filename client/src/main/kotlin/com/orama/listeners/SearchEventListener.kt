package com.orama.listeners

import com.orama.model.search.SearchResponse

interface SearchEventListener {
    fun onComplete(results: SearchResponse) {}
    fun onError(error: String) {}
}