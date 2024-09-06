package com.orama.model.index

import kotlinx.serialization.Serializable

@Serializable
data class DeleteRequest (
    val delete: List<String>
)
