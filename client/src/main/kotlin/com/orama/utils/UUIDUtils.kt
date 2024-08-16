package com.orama.utils

import java.util.UUID

object UUIDUtils {
    fun generate(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}