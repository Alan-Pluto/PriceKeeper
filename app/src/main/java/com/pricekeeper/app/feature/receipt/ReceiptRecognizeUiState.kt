package com.pricekeeper.app.feature.receipt

sealed interface ReceiptRecognizeUiState {
    data object Loading : ReceiptRecognizeUiState
    data class Success(
        val items: List<EditableReceiptItem>,
        val storeHint: String? = null,
        val totalHint: Double? = null
    ) : ReceiptRecognizeUiState
    data class Error(val message: String) : ReceiptRecognizeUiState
}
