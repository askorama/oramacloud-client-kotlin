package com.orama.utils

import com.benasher44.uuid.uuid4

object UUIDUtils {
    fun generate(): String {
        return uuid4().toString().replace("-", "")
    }
}