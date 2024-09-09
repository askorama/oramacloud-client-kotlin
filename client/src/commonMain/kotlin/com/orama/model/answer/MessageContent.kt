package com.orama.model.answer

import com.orama.model.search.Hit
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonElement

@Serializable
sealed class MessageContent

@Serializable
@SerialName("StringMessage")
data class StringMessage(val content: String) : MessageContent()

@Serializable
@SerialName("TranslatedQueryMap")
data class TranslatedQuery(val content: Map<String, JsonElement>) : MessageContent()

@Serializable
@SerialName("SourcesList")
data class SourcesList<T>(val content: List<Hit<T>>) : MessageContent()
