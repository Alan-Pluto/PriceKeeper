package com.pricekeeper.app.feature.receipt

import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptRecognizeScreen(
    onConfirmSave: (List<EditableReceiptItem>, String?, Double?) -> Unit,
    onBack: () -> Unit,
    viewModel: ReceiptRecognizeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PriceKeeperTopBar(
                title = "识别结果",
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        bottomBar = {
            when (val state = uiState) {
                is ReceiptRecognizeUiState.Success -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            "识别到 ${state.items.size} 项商品",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onConfirmSave(state.items, state.storeHint, state.totalHint) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("确认存档")
                        }
                    }
                }
                else -> {}
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is ReceiptRecognizeUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("正在识别...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            is ReceiptRecognizeUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = viewModel::retry) { Text("重试") }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onBack) { Text("返回") }
                    }
                }
            }

            is ReceiptRecognizeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ReceiptItemEditRow(
                            item = item,
                            onNameChange = { viewModel.onItemNameChange(item.id, it) },
                            onPriceChange = { viewModel.onItemPriceChange(item.id, it) },
                            onDelete = { viewModel.onDeleteItem(item.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) } // space for bottom bar
                }
            }
        }
    }
}
