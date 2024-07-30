package com.orama

import com.orama.client.OramaClient
import com.orama.model.search.SearchParams
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime

class OramaClientTest {

    @OptIn(ExperimentalTime::class)
    @Test fun testClient() = runBlocking {
        val client = OramaClient(
            endpoint = "https://cloud.orama.run/v1/indexes/orama-docs-bzo330",
            apiKey = "NKiqTJnwnKsQCdxN7RyOBJgeoW5hJ594"
        )

        val searchParams = SearchParams.builder(
            term = "typescript",
            mode = SearchParams.Mode.FULLTEXT
        )
            .where(listOf(
                SearchParams.Condition("category", SearchParams.ConditionType.Equals("Cloud")),
            ))
            .build()

        val result = CompletableDeferred<Unit>()

        try {
            withTimeout(10000) {
                client.search(searchParams) { response, error ->
                    assertNull(error)
                    assertNotNull(response)
                    result.complete(Unit)
                }
                result.await()
            }
        } catch (e: Exception) {
            println("Request timed out or an error occurred: ${e.message}")
        }
    }
}
