package com.orama.model.answer

import com.orama.client.OramaClient
import kotlinx.serialization.KSerializer

data class AnswerParams<T>(
    val oramaClient: OramaClient,
    val userContext: String? = null,
    val inferenceType: InferenceType? = InferenceType.DOCUMENTATION,
    val initialMessages: MutableList<Message> = mutableListOf(),
    val serializer: KSerializer<T>
)
