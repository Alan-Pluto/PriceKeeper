package com.pricekeeper.app.feature.receipt

/** Camera vs Gallery capture mode. */
enum class CaptureMode { CAMERA, GALLERY }

data class ReceiptCaptureUiState(
    val captureMode: CaptureMode = CaptureMode.CAMERA,
    val imagePath: String? = null,
    val hasCameraPermission: Boolean = false,
    val isLoading: Boolean = false
)
