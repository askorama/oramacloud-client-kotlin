package com.orama.endpoint

import com.orama.model.search.SearchResponse
import com.orama.model.search.SearchParams
import com.orama.client.OramaClient
import okhttp3.*
import java.io.IOException
import kotlinx.serialization.json.*
import java.util.*

sealed class Search() {
    companion object {
        val jsonDeserializer = Json {
            ignoreUnknownKeys = true
        }

        fun get(
            oramaClient: OramaClient,
            httpClient: OkHttpClient,
            searchParams: SearchParams,
            callback: (response: SearchResponse?, error: Exception?) -> Unit
        ) {
            val requestBody = FormBody.Builder()
                .addEncoded("q", searchParams.toJson())
                .addEncoded("version", "1.3.2")
                .addEncoded("id", UUID.randomUUID().toString().replace("-", " "))
                .build()

            val request = Request.Builder()
                .url("${oramaClient.endpoint}/search?api-key=${oramaClient.apiKey}")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build()

                httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback(null, e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        if (response.isSuccessful && responseBody != null) {
                            callback(jsonDeserializer.decodeFromString<SearchResponse>(responseBody), null)
                        } else {
                            callback(null, IOException(response.message))
                        }
                    }
                })
        }
    }
}