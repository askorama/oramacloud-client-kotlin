package com.orama.endpoint

import com.orama.listeners.IndexManagerEventListener
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

@Serializable
data class UpsertRequest<T> (
    val upsert: List<T>
)

@Serializable
data class DeleteRequest (
    val delete: List<String>
)

public enum class IndexMethod(public val value: String) {
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
    index: String,
    private val events: IndexManagerEventListener?
) {
    private val json = Json { prettyPrint = true }
    private val transport = Transport(client, index)

    suspend fun snapshot(documents: List<T>, serializer: KSerializer<T>) = handleException(
        onSuccess = { events?.onSnapshotSuccess() },
        onError = { error -> events?.onSnapshotError(error) }
    ) {
        val snapshotData = json.encodeToString(ListSerializer(serializer), documents)
        transport.post(IndexMethod.SNAPSHOT, snapshotData)
    }

    private suspend fun insertOrUpdate(documents: List<T>, serializer: KSerializer<T>) {
        val upsertData = UpsertRequest(documents)
        val snapshotData = json.encodeToString(UpsertRequest.serializer(serializer), upsertData)
        transport.post(IndexMethod.NOTIFY, snapshotData)
    }

    suspend fun insert(documents: List<T>, serializer: KSerializer<T>) = handleException(
        onSuccess = { events?.onInsertSuccess() },
        onError = { e -> events?.onInsertError(e) }
    ) {
        insertOrUpdate(documents, serializer)
    }

     suspend fun update(documents: List<T>, serializer: KSerializer<T>) = handleException(
        onSuccess = { events?.onUpdateSuccess() },
        onError = { e -> events?.onUpdateError(e) }
    ) {
        insertOrUpdate(documents, serializer)
    }

    suspend fun delete(ids: List<String>) = handleException(
        onSuccess = { events?.onDeleteSuccess() },
        onError = { e -> events?.onDeleteError(e) }
    ) {
        val deleteData = json.encodeToString(DeleteRequest.serializer(), DeleteRequest(ids))
        transport.post(IndexMethod.NOTIFY, deleteData)
    }

    suspend fun deploy() = handleException(
        onSuccess = { events?.onDeploySuccess() },
        onError = { e -> events?.onDeployError(e) }
    ) {
        transport.post(IndexMethod.DEPLOY)
    }

    suspend fun clear() = handleException(
        onSuccess = { events?.onClearSuccess() },
        onError = { e -> events?.onClearError(e) }
    ) {
        transport.post(IndexMethod.SNAPSHOT, emptyList<T>())
    }

    suspend fun hasPendingOperations() = handleException(
        onSuccess = { events?.onCheckPendingSuccess(true) },
        onError = { e -> events?.onCheckPendingSuccess(false) }
    ) {
        transport.post(IndexMethod.HAS_DATA)
    }

    private suspend fun handleException(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        block: suspend () -> Unit
    ) {
        try {
            block()
            onSuccess()
        } catch (error: Exception) {
            onError(error)
        }
    }
}