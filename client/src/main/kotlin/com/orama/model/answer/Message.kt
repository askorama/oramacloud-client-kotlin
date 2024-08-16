package com.orama.model.answer

import kotlinx.serialization.Serializable

@Serializable
data class Message (
    val role: Role,
    val content: String
)