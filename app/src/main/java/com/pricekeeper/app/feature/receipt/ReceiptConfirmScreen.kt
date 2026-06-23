package com.pricekeeper.app.feature.receipt

import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun ReceiptConfirmScreen(
    imagePath: String,
    items: List<EditableReceiptItem>,
    storeHint: String?,
    totalHint: Double?,
    onSave: suspend (storeName: String, storeRegion: String) -> Boolean,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var storeName by remember { mutableStateOf(storeHint ?: "") }
    var storeRegion by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showStoreSheet by remember { mutableStateOf(false) }

    // Store selection bottom sheet
    if (showStoreSheet) {
        StoreSelectBottomSheet(
            recentStores = emptyList(),
            onStoreSelected = { store ->
                storeName = store.name
                storeRegion = store.region
                showStoreSheet = false
            },
            onNewStore = { name ->
                storeName = name
                showStoreSheet = false
            },
            onDismiss = { showStoreSheet = false }
        )
    }

    Scaffold(
        topBar = {
            PriceKeeperTopBar(
                title = "确认存档",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = {
                        if (storeName.isNotBlank()) {
                            isSaving = true
                            scope.launch {
                                onSave(storeName, storeRegion)
                                isSaving = false
                            }
                        }
                    },
                    enabled = storeName.isNotBlank() && !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSaving) "保存中..." else "确认存档 (${items.size} 项)")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Store name
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("这次在哪家商店买的？") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Region
            OutlinedTextField(
                value = storeRegion,
                onValueChange = { storeRegion = it },
                label = { Text("所在区域（选填）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Total hint
            totalHint?.let { total ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        "合计: ¥%.2f".format(total),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Items summary
            Text("待保存商品 (${items.size}项):", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items, key = { it.id }) { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(item.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text("¥${item.price}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
