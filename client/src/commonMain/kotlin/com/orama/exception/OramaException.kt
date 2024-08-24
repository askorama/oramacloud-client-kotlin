package com.orama.exception

class OramaException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)