package com.pricekeeper.app.feature.receipt

import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptCaptureScreen(
    onImageReady: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ReceiptCaptureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
        if (!granted) {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onGalleryImageSelected(it) }
    }

    // Navigate when image is captured
    val imagePath = uiState.imagePath
    if (imagePath != null) {
        onImageReady(imagePath)
        return
    }

    Scaffold(
        topBar = {
            PriceKeeperTopBar(
                title = "拍小票",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        floatingActionButton = {
            if (uiState.captureMode == CaptureMode.CAMERA) {
                FloatingActionButton(
                    onClick = {
                        if (!uiState.hasCameraPermission) {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        // Camera capture via shutter — handled by PreviewView
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "拍照")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mode tabs: Camera | Gallery
            TabRow(selectedTabIndex = if (uiState.captureMode == CaptureMode.CAMERA) 0 else 1) {
                Tab(
                    selected = uiState.captureMode == CaptureMode.CAMERA,
                    onClick = { viewModel.onModeChange(CaptureMode.CAMERA) },
                    text = { Text("拍照") },
                    icon = { Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp)) }
                )
                Tab(
                    selected = uiState.captureMode == CaptureMode.GALLERY,
                    onClick = {
                        viewModel.onModeChange(CaptureMode.GALLERY)
                        galleryLauncher.launch("image/*")
                    },
                    text = { Text("相册") },
                    icon = { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)) }
                )
            }

            // Content area
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator()
                    uiState.captureMode == CaptureMode.CAMERA && !uiState.hasCameraPermission -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("需要相机权限", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "请授予相机权限后拍照",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    uiState.captureMode == CaptureMode.CAMERA -> {
                        CameraPreview(
                            onImageCaptured = { path ->
                                viewModel.onImageCaptured(path)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Text(
                            "点击上方相册选择图片",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
