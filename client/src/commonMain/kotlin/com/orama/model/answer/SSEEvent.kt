package com.orama.model.answer

import kotlinx.serialization.Serializable

@Serializable
data class SSEEvent<T> (
    val type: EventType?,
    val interactionId: String?,
    val message: MessageContent?
)