package com.orama.endpoint

import com.orama.model.index.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

enum class IndexMethod(val value: String) {
    SNAPSHOT("snapshot"),
    DEPLOY("deploy"),
    NOTIFY("notify"),
    HAS_DATA("has-data")
}

class Transport(private val client: HttpClient, private val index: String) {
    suspend fun post(indexMethod: IndexMethod, data: Any? = null): HttpResponse {
        return client.post("${index}/${indexMethod.value}") {
            contentType(ContentType.Application.Json)
            setBody(data ?: "[]")
        }
    }
}

class IndexManager<T : Any>(
    client: HttpClient,
    index: String
) {
    private val json = Json { prettyPrint = true }
    private val transport = Transport(client, index)

    suspend fun snapshot(documents: List<T>, serializer: KSerializer<T>): Boolean {
        try {
            val snapshotData = json.encodeToString(ListSerializer(serializer), documents)
            val httpResponse = transport.post(IndexMethod.SNAPSHOT, snapshotData)
            val delete = json.decodeFromString<UpsertResponse>(httpResponse.bodyAsText())
            return delete.success
        } catch (e: Exception) {
            throw e;
        }
    }

    private suspend fun insertOrUpdate(documents: List<T>, serializer: KSerializer<T>): Boolean {
        try {
            val upsertData = UpsertRequest(documents)
            val snapshotData = json.encodeToString(UpsertRequest.serializer(serializer), upsertData)
            val httpResponse = transport.post(IndexMethod.NOTIFY, snapshotData)
            val upsert = json.decodeFromString<UpsertResponse>(httpResponse.bodyAsText())
            return upsert.success
        }  catch (e: Exception) {
            throw e;
        }
    }

    suspend fun insert(documents: List<T>, serializer: KSerializer<T>): Boolean {
        return this.insertOrUpdate(documents, serializer)
    }

    suspend fun update(documents: List<T>, serializer: KSerializer<T>): Boolean {
         return this.insertOrUpdate(documents, serializer)
    }

    suspend fun delete(ids: List<String>): Boolean {
        try {
            val deleteData = json.encodeToString(DeleteRequest.serializer(), DeleteRequest(ids))
            val httpResponse = transport.post(IndexMethod.NOTIFY, deleteData)
            val delete = json.decodeFromString<UpsertResponse>(httpResponse.bodyAsText())
            return delete.success
        } catch (e: Exception) {
            throw e;
        }
    }

    suspend fun deploy(): String {
        try {
            val httpResponse = transport.post(IndexMethod.DEPLOY)
            val deploy = json.decodeFromString<DeployResponse>(httpResponse.bodyAsText())
            return deploy.deploymentId
        } catch (e: Exception) {
            throw e;
        }
    }

    suspend fun clear(): Boolean {
        try {
            val httpResponse = transport.post(IndexMethod.SNAPSHOT, "[]")
            val clear = json.decodeFromString<UpsertResponse>(httpResponse.bodyAsText())
            return clear.success
        } catch (e: Exception) {
            throw e;
        }
    }

    suspend fun hasPendingOperations(): Boolean {
        try {
            val httpResponse = transport.post(IndexMethod.HAS_DATA)
            val checkOperation = json.decodeFromString<HasPendingOperations>(httpResponse.bodyAsText())
            return checkOperation.hasData
        } catch (e: Exception) {
            throw e;
        }
    }
}