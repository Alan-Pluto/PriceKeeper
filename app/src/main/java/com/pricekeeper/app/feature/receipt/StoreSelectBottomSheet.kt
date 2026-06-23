package com.pricekeeper.app.feature.receipt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pricekeeper.app.domain.model.Store

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSelectBottomSheet(
    recentStores: List<Store>,
    onStoreSelected: (Store) -> Unit,
    onNewStore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var searchQuery by remember { mutableStateOf("") }
    var newStoreName by remember { mutableStateOf("") }

    val filteredStores = remember(searchQuery, recentStores) {
        if (searchQuery.isBlank()) recentStores
        else recentStores.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("这次在哪家商店买的？", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索已有商店") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Recent stores list
            LazyColumn(modifier = Modifier.height(200.dp)) {
                items(filteredStores, key = { it.id }) { store ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStoreSelected(store) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(store.name, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Text(
                            store.region,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    HorizontalDivider()
                }
            }

            Spacer(Modifier.height(12.dp))

            // New store input
            OutlinedTextField(
                value = newStoreName,
                onValueChange = { newStoreName = it },
                label = { Text("新增商店") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { if (newStoreName.isNotBlank()) onNewStore(newStoreName.trim()) },
                enabled = newStoreName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("新增商店并保存")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
