package com.pricekeeper.app.feature.receipt

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executors

/** Tag keys for storing data in PreviewView. */
private const val TAG_IMAGE_CAPTURE = 0x70000001
private const val TAG_CAPTURE_CALLBACK = 0x70000002
private const val TAG_CAPTURE_FILE = 0x70000003

/** Shared executor for camera operations. */
private val cameraExecutor = Executors.newSingleThreadExecutor()

/**
 * Composable wrapper around CameraX PreviewView.
 */
@Composable
fun CameraPreview(
    onImageCaptured: (String) -> Unit,
    modifier: Modifier = Modifier,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val outputFile = remember { ReceiptImageUtil.createTempImageFile(context) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCapture = ImageCapture.Builder()
                    .setTargetRotation(0) // ROTATION_0
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )

                    // Store references via setTag (using integer keys in app-private range)
                    previewView.setTag(TAG_IMAGE_CAPTURE, imageCapture)
                    previewView.setTag(TAG_CAPTURE_CALLBACK, onImageCaptured)
                    previewView.setTag(TAG_CAPTURE_FILE, outputFile)
                } catch (_: Exception) { }
            }, ContextCompat.getMainExecutor(context))
        },
        modifier = modifier
    )
}

/**
 * Take a photo using the currently bound camera. Call from a shutter button click.
 */
fun takePhoto(previewView: PreviewView): Boolean {
    val imageCapture = previewView.getTag(TAG_IMAGE_CAPTURE) as? ImageCapture ?: return false
    val callback = previewView.getTag(TAG_CAPTURE_CALLBACK) as? ((String) -> Unit) ?: return false
    val file = previewView.getTag(TAG_CAPTURE_FILE) as? File ?: return false

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                callback(file.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                // Error handled silently — user retries
            }
        }
    )
    return true
}
