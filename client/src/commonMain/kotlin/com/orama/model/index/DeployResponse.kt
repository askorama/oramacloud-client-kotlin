package com.orama.model.index

import kotlinx.serialization.Serializable

@Serializable
data class DeployResponse(
    val deploymentId: String
)