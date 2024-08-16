package com.orama.client

import com.orama.endpoint.IndexManager
import com.orama.listeners.IndexManagerEventListener
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*

class CustomHttpLogger(): Logger {
    override fun log(message: String) {
        println("loggerTag $message")
    }
}

class CloudManager(private val apiKey: String) {
    val webhookUrl = "https://api.oramasearch.com/api/v1/webhooks/"

    internal val client = HttpClient(CIO) {
        install(Logging) {
            logger = CustomHttpLogger()
            level = LogLevel.ALL
        }
        defaultRequest {
            url(webhookUrl)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header(HttpHeaders.ContentType, "application/json")
        }
    }

    fun <T: Any> index(index: String, events: IndexManagerEventListener? = null): IndexManager<T> {
       return IndexManager<T>(client, index, events)
   }
}