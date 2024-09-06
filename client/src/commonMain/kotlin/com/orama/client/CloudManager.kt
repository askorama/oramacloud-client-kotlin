package com.orama.client

import com.orama.endpoint.IndexManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

class CloudManager(private val apiKey: String) {
    private val webhookUrl = "https://api.oramasearch.com/api/v1/webhooks/"

    internal val client = HttpClient(CIO) {
        defaultRequest {
            url(webhookUrl)
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header(HttpHeaders.ContentType, "application/json")
        }
    }

    fun <T: Any> index(index: String): IndexManager<T> {
       return IndexManager<T>(client, index)
   }
}