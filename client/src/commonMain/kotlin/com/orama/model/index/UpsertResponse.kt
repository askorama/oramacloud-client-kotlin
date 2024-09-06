package com.orama.model.index

import kotlinx.serialization.Serializable

@Serializable
data class UpsertResponse(
    val success: Boolean
)