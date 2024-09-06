package com.orama.model.index

import kotlinx.serialization.Serializable

@Serializable
data class HasPendingOperations(
    val hasData: Boolean
)