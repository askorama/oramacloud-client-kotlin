package com.orama.endpoint

import com.orama.model.search.SearchResponse
import com.orama.model.search.SearchParams
import com.orama.client.OramaClient
import com.orama.exception.OramaException
import com.orama.utils.UUIDUtils
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

sealed class Search {
    companion object {
        private val jsonDeserializer = Json {
            ignoreUnknownKeys = true
        }

        suspend fun <T> get(
            oramaClient: OramaClient,
            httpClient: HttpClient,
            searchParams: SearchParams,
            serializer: KSerializer<T>
        ): SearchResponse<T> {
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
                    // Witin SesarchResponse the "hit" property should be serialized with the given serializer.
                    return jsonDeserializer.decodeFromString(SearchResponse.serializer(serializer), responseBody)
                } else {
                    throw OramaException(
                        "Error while parsing response: ${response.status.description}"
                    )
                }
            } catch (exception: Exception) {
                throw OramaException(exception.message, exception)
            }
        }
    }
}