package com.orama.endpoint

import com.orama.model.search.SearchResponse
import com.orama.model.search.SearchParams
import com.orama.client.OramaClient
import com.orama.listeners.SearchEventListener
import com.orama.utils.UUIDUtils
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

sealed class Search {
    companion object {
        private val jsonDeserializer = Json {
            ignoreUnknownKeys = true
        }

        suspend fun get(
            oramaClient: OramaClient,
            httpClient: HttpClient,
            searchParams: SearchParams,
            events: SearchEventListener?
        ) {
            try {
                val response: HttpResponse = httpClient.submitForm(
                    url = "${oramaClient.endpoint}/search",
                    formParameters = Parameters.build {
                        append("q", searchParams.toJson())
                        append("version", "kotlin-1.0.0") // TODO: replace for buildconfig
                        append("id", UUIDUtils.generate())
                    },
                    encodeInQuery = false
                ) {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    parameter("api-key", oramaClient.apiKey)
                }

                val responseBody = response.bodyAsText()
                if (response.status.isSuccess()) {
                    val searchResponse = jsonDeserializer.decodeFromString<SearchResponse>(responseBody)
                    events?.onComplete(searchResponse)
                } else {
                    events?.onError(response.status.description)
                }
            } catch (e: Exception) {
                events?.onError(e.message ?: "unknown error")
            }
        }
    }
}