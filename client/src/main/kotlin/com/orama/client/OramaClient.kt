/*
 * This source file was generated by the Gradle 'init' task
 */
package com.orama.client

import com.orama.model.search.SearchResponse
import com.orama.model.search.SearchParams
import com.orama.endpoint.Search
import okhttp3.OkHttpClient

class OramaClient(var apiKey: String, var endpoint: String) {
    private val httpClient = OkHttpClient()

    fun search(searchParams: SearchParams, callback: (response: SearchResponse?, error: Exception?) -> Unit) {
        return Search.get(this@OramaClient, httpClient, searchParams, callback)
    }
}
