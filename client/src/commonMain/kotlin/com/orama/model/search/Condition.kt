package com.orama.model.search

import kotlinx.serialization.Serializable

@Serializable
data class Condition(val field: String, val condition: ConditionType) {

}