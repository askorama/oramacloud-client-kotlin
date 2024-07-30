package com.orama.model.search

import kotlinx.serialization.Serializable

@Serializable
data class Elapsed(
    val raw: Int,
    val formatted: String
)