package com.orama.model.index

import kotlinx.serialization.Serializable

@Serializable
data class UpsertRequest<T> (
    val upsert: List<T>
)