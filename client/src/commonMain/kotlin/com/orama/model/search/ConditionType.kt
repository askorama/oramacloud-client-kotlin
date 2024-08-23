package com.orama.model.search

import kotlinx.serialization.Serializable

@Serializable
sealed class ConditionType {
    data class GreaterThan(val value: Double) : ConditionType()
    data class GreaterThanOrEqual(val value: Double) : ConditionType()
    data class LessThan(val value: Double) : ConditionType()
    data class LessThanOrEqual(val value: Double) : ConditionType()
    data class Between(val value: List<Double>) : ConditionType()

    data class Equals(val value: String) : ConditionType()
    data class In(val value: List<String>) : ConditionType()
    data class Nin(val value: List<String>) : ConditionType()
    data class ContainsAll(val value: List<String>) : ConditionType()
}