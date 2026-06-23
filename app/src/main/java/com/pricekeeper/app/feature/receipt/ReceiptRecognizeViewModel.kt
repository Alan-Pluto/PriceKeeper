package com.pricekeeper.app.feature.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pricekeeper.app.data.ocr.ImageCompressor
import com.pricekeeper.app.data.ocr.OcrEngine
import com.pricekeeper.app.data.parser.ReceiptParser
import com.pricekeeper.app.domain.model.ParsedReceiptItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReceiptRecognizeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ocrEngine: OcrEngine,
    private val parser: ReceiptParser,
    private val imageCompressor: ImageCompressor
) : ViewModel() {

    private val imagePath: String = savedStateHandle.get<String>("imagePath") ?: ""

    private val _uiState = MutableStateFlow<ReceiptRecognizeUiState>(ReceiptRecognizeUiState.Loading)
    val uiState: StateFlow<ReceiptRecognizeUiState> = _uiState.asStateFlow()

    init {
        recognizeAndParse()
    }

    private fun recognizeAndParse() {
        viewModelScope.launch {
            _uiState.value = ReceiptRecognizeUiState.Loading
            try {
                // Compress image first
                val compressedPath = "${imagePath}_compressed.jpg"
                withContext(Dispatchers.IO) {
                    imageCompressor.compress(imagePath, compressedPath)
                }

                // OCR
                val ocrResult = ocrEngine.recognize(compressedPath)

                ocrResult.fold(
                    onSuccess = { rawText ->
                        if (rawText.isBlank()) {
                            _uiState.value = ReceiptRecognizeUiState.Error(
                                "未识别到有效商品信息，请尝试手动录入"
                            )
                            return@launch
                        }

                        // Parse
                        val parseResult = withContext(Dispatchers.Default) {
                            parser.parse(rawText)
                        }

                        if (parseResult.pricedItemCount == 0) {
                            _uiState.value = ReceiptRecognizeUiState.Error(
                                "未识别到有效商品信息，请尝试手动录入"
                            )
                            return@launch
                        }

                        val editableItems = parseResult.pricedItems.map { it.toEditable() }
                        _uiState.value = ReceiptRecognizeUiState.Success(
                            items = editableItems,
                            storeHint = parseResult.storeNameHint,
                            totalHint = parseResult.totalPrice
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = ReceiptRecognizeUiState.Error(
                            error.message ?: "OCR 识别失败，请重试"
                        )
                    }
                )

                // Cleanup compressed file
                File(compressedPath).delete()
            } catch (e: Exception) {
                _uiState.value = ReceiptRecognizeUiState.Error(
                    e.message ?: "识别过程出错，请重试"
                )
            }
        }
    }

    fun onItemNameChange(itemId: String, name: String) {
        _uiState.update { state ->
            if (state is ReceiptRecognizeUiState.Success) {
                state.copy(
                    items = state.items.map {
                        if (it.id == itemId) it.copy(name = name, isEdited = true) else it
                    }
                )
            } else state
        }
    }

    fun onItemPriceChange(itemId: String, price: String) {
        _uiState.update { state ->
            if (state is ReceiptRecognizeUiState.Success) {
                state.copy(
                    items = state.items.map {
                        if (it.id == itemId) it.copy(price = price, isEdited = true) else it
                    }
                )
            } else state
        }
    }

    fun onItemCategoryChange(itemId: String, category: String) {
        _uiState.update { state ->
            if (state is ReceiptRecognizeUiState.Success) {
                state.copy(
                    items = state.items.map {
                        if (it.id == itemId) it.copy(category = category, isEdited = true) else it
                    }
                )
            } else state
        }
    }

    fun onDeleteItem(itemId: String) {
        _uiState.update { state ->
            if (state is ReceiptRecognizeUiState.Success) {
                state.copy(items = state.items.filter { it.id != itemId })
            } else state
        }
    }

    /** Retry recognition (used from error state). */
    fun retry() {
        recognizeAndParse()
    }

    /** Get the current list of parsed items for saving. */
    fun getFinalItems(): List<ParsedReceiptItem> {
        val state = _uiState.value
        return if (state is ReceiptRecognizeUiState.Success) {
            state.items.map {
                ParsedReceiptItem(name = it.name, price = it.price.toDoubleOrNull())
            }
        } else emptyList()
    }

    private fun ParsedReceiptItem.toEditable() = EditableReceiptItem(
        name = name,
        price = price?.toString() ?: "",
        category = "未分类"
    )
}
