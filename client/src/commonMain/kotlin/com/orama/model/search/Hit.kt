package com.orama.model.search

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Hit<T>(
    val id: String,
    val score: Double,
    val document: T
)
