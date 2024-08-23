package com.orama.model.answer

import com.orama.client.OramaClient

data class AnswerParams(
    val oramaClient: OramaClient,
    val userContext: String? = null,
    val inferenceType: InferenceType? = InferenceType.DOCUMENTATION,
    val initialMessages: MutableList<Message> = mutableListOf(),
)
