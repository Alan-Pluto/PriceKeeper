package com.pricekeeper.app.feature.profile

import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onBack: () -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deleteConfirmCategory by remember { mutableStateOf<String?>(null) }

    // Delete confirmation dialog
    deleteConfirmCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deleteConfirmCategory = null },
            title = { Text("删除分类") },
            text = { Text("确定删除 [$category]？\n该分类下的商品将被移至「未分类」。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
                    deleteConfirmCategory = null
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmCategory = null }) { Text("取消") }
            }
        )
    }

    // Add category dialog
    if (uiState.isAdding) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelAdd() },
            title = { Text("添加分类") },
            text = {
                OutlinedTextField(
                    value = uiState.newCategoryName,
                    onValueChange = viewModel::onNewCategoryNameChange,
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmAdd,
                    enabled = uiState.newCategoryName.isNotBlank()
                ) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelAdd) { Text("取消") }
            }
        )
    }

    // Edit category dialog
    uiState.editingCategory?.let { _ ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelEdit() },
            title = { Text("重命名分类") },
            text = {
                OutlinedTextField(
                    value = uiState.editName,
                    onValueChange = viewModel::onEditNameChange,
                    label = { Text("新名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmEdit,
                    enabled = uiState.editName.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelEdit) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            PriceKeeperTopBar(
                title = "分类管理",
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
            FloatingActionButton(onClick = viewModel::startAdd) {
                Icon(Icons.Default.Add, "添加分类")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("加载中...")
            }
        } else if (uiState.categories.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无分类", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(uiState.categories, key = { it }) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            category,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.startEdit(category) }) {
                            Icon(Icons.Default.Edit, "重命名",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        if (category != "未分类") {
                            IconButton(onClick = { deleteConfirmCategory = category }) {
                                Icon(Icons.Default.Delete, "删除",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
