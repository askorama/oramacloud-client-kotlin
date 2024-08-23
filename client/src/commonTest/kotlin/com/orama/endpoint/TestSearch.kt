package com.orama.endpoint

import com.orama.client.OramaClient
import com.orama.listeners.SearchEventListener
import com.orama.model.search.Mode
import com.orama.model.search.SearchParams
import com.orama.model.search.SearchResponse
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.assertEquals
import kotlin.test.Test

class TestSearch {
    private val exampleSearchResponse = """{
        "count": 1,
        "elapsed": {"raw": 0, "formatted": "0ms"},
        "hits": [{
            "id": "233",
            "score": 5.787301233651877,
            "document": {
                "category": "Cloud",
                "content": "You can install the JavaScript SDK via any major package manager. ",
                "id": "233",
                "path": "/cloud/performing-search/official-sdk#javascript",
                "section": "Performing Search",
                "title": "JavaScript"
            }
        }],
        "facets": {
            "section": {
                "count": 1,
                "values": {"Performing Search": 1}
            }
        }
    }"""

    private fun exampleSearchParams(): SearchParams {
        return SearchParams.builder(
            term = "red shoes",
            mode = Mode.FULLTEXT
        )
            .limit(10)
            .offset(0)
            .returning(listOf("title", "description"))
            .build()
    }

    @Test
    fun getSearchResults() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = exampleSearchResponse,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }

        val mockHttpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val oramaClient = OramaClient(
            apiKey = "some-api-key",
            endpoint = "https://oramacloud.com/some-endpoint"
        )

        Search.get(
            oramaClient,
            mockHttpClient,
            exampleSearchParams(),
            events = object : SearchEventListener {
                override fun onError(error: String) {
                    // Handle error
                }

                override fun onComplete(results: SearchResponse) {
                    val hit = results.hits[0]
                    assertEquals(5.787301233651877, hit.score)
                    assertEquals("233", hit.id)
                    assertEquals("Cloud", (hit.document["category"] as? JsonPrimitive)?.content)
                    assertEquals("/cloud/performing-search/official-sdk#javascript", (hit.document["path"] as? JsonPrimitive)?.content)
                    assertEquals("Performing Search", (hit.document["section"] as? JsonPrimitive)?.content)
                    assertEquals("JavaScript", (hit.document["title"] as? JsonPrimitive)?.content)
                    assertEquals("You can install the JavaScript SDK via any major package manager. ", (hit.document["content"] as? JsonPrimitive)?.content)
                }
            }
        )
    }
}