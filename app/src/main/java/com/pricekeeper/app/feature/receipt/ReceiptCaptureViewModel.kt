package com.pricekeeper.app.feature.receipt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReceiptCaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptCaptureUiState())
    val uiState: StateFlow<ReceiptCaptureUiState> = _uiState.asStateFlow()

    init {
        checkCameraPermission()
    }

    fun onModeChange(mode: CaptureMode) {
        _uiState.update { it.copy(captureMode = mode) }
        if (mode == CaptureMode.CAMERA) checkCameraPermission()
    }

    fun onImageCaptured(path: String) {
        _uiState.update { it.copy(imagePath = path) }
    }

    fun onGalleryImageSelected(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dest = ReceiptImageUtil.createTempImageFile(context)
                val path = ReceiptImageUtil.copyUriToFile(context, uri, dest)
                _uiState.update { it.copy(imagePath = path, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Returns the current image path to pass to navigation, null if still capturing. */
    fun getImagePath(): String? = _uiState.value.imagePath

    private fun checkCameraPermission() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }
}
