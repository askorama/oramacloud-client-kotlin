package com.orama.model.search

import kotlinx.serialization.SerialName

enum class Mode {
    @SerialName("fulltext") FULLTEXT,
    @SerialName("vector") VECTOR,
    @SerialName("hybrid") HYBRID
}