package com.orama.listeners

interface IndexManagerEventListener {
    fun onSnapshotSuccess() {}
    fun onSnapshotError(exception: Exception) {}
    fun onInsertSuccess() {}
    fun onInsertError(exception: Exception) {}
    fun onUpdateSuccess() {}
    fun onUpdateError(exception: Exception) {}
    fun onDeleteSuccess() {}
    fun onDeleteError(exception: Exception) {}
    fun onDeploySuccess() {}
    fun onDeployError(exception: Exception) {}
    fun onClearSuccess() {}
    fun onClearError(exception: Exception) {}
    fun onError(exception: Exception) {}
    fun onCheckPendingSuccess(hasPending: Boolean) {}
    fun onCheckPendingError(exception: Exception) {}
}