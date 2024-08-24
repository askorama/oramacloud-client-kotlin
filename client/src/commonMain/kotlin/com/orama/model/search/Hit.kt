package com.orama.model.search

import kotlinx.serialization.Serializable

@Serializable
data class Hit<T>(
    val id: String,
    val score: Double,
    val document: T
)
