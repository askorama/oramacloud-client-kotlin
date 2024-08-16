package com.orama.model.answer

data class AskParams (
    val query: String,
    val userData: String? = null,
    val related: String? = null,
    val messages: List<Message>? = null,
    val searchParams: Map<String, String> = emptyMap()
)