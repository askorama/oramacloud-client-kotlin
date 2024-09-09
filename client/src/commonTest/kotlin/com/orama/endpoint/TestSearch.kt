package com.orama.endpoint

import com.orama.client.OramaClient
import com.orama.model.search.Mode
import com.orama.model.search.SearchParams
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.Test

class TestSearch {
    @Serializable
    data class MyDoc (
        val title: String,
        val category: String,
        val path: String,
        val content: String,
        val section: String
    )

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

        val results = Search.get(
            oramaClient,
            mockHttpClient,
            exampleSearchParams(),
            MyDoc.serializer()
        )

        val hit = results.hits[0]
        assertEquals(5.787301233651877, hit.score)
        assertEquals("233", hit.id)

        val document = hit.document
        assertEquals("Cloud", document.category)
        assertEquals("/cloud/performing-search/official-sdk#javascript", document.path)
        assertEquals("Performing Search", document.section)
        assertEquals("JavaScript", document.title)
        assertEquals("You can install the JavaScript SDK via any major package manager. ", document.content)
    }
}