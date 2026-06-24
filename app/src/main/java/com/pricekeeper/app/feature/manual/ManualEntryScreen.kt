package com.pricekeeper.app.feature.manual

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pricekeeper.app.core.location.parseStoreCoordinates
import com.pricekeeper.app.core.location.resolveStoreLocation
import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onSaveAndBack: () -> Unit,
    viewModel: ManualEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val mapPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        val location = parseStoreCoordinates(uri)
        if (location == null) {
            Toast.makeText(context, "未获取到地图选点，请确认地图应用支持返回坐标", Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }
        val locationInfo = resolveStoreLocation(context, location.first, location.second)
        viewModel.onStoreLocationSelected(
            latitude = locationInfo.latitude,
            longitude = locationInfo.longitude,
            region = locationInfo.region,
            address = locationInfo.address,
            mapUrl = uri.toString()
        )
    }

    if (uiState.showCategorySheet) {
        CategoryPickerSheet(
            query = uiState.categorySearchQuery,
            categories = uiState.filteredCategories,
            canCreateCategory = uiState.categorySearchQuery.isNotBlank() &&
                uiState.categories.none { it == uiState.categorySearchQuery.trim() },
            onQueryChange = viewModel::onCategorySearchQueryChange,
            onCategorySelected = viewModel::onCategoryChange,
            onCreateCategory = { viewModel.onCategoryChange(uiState.categorySearchQuery.trim()) },
            onDismiss = { viewModel.onCategorySheetVisibleChange(false) }
        )
    }
    if (uiState.showStoreSheet) {
        StorePickerSheet(
            query = uiState.storeSearchQuery,
            stores = uiState.filteredStores,
            onQueryChange = viewModel::onStoreSearchQueryChange,
            onStoreSelected = viewModel::onStoreSelected,
            onNewStore = viewModel::onNewStoreToggle,
            onDismiss = { viewModel.onStoreSheetVisibleChange(false) }
        )
    }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess && uiState.pendingBackAfterSave) {
            viewModel.onSaveNavigationConsumed()
            onSaveAndBack()
        }
    }
    LaunchedEffect(uiState.storeLocationError) {
        if (uiState.showNewStoreFields && uiState.storeLocationError == "请先粘贴地图位置分享链接") {
            Toast.makeText(context, "请先粘贴地图位置分享链接", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            PriceKeeperTopBar(
                title = "新增物价记录",
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        bottomBar = {
            ManualEntryBottomBar(
                isSaving = uiState.isSaving,
                onSaveAndContinue = {
                    viewModel.saveAndContinue { latitude, longitude ->
                        withContext(Dispatchers.IO) {
                            resolveStoreLocation(context, latitude, longitude)
                        }
                    }
                },
                onSaveOnly = {
                    viewModel.saveOnly { latitude, longitude ->
                        withContext(Dispatchers.IO) {
                            resolveStoreLocation(context, latitude, longitude)
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GoodsAndPriceCard(uiState = uiState, viewModel = viewModel)
            }
            item {
                CategoryAndStoreCard(uiState = uiState, viewModel = viewModel)
            }
            if (uiState.showNewStoreFields) {
                item {
                    NewStoreLocationCard(
                        uiState = uiState,
                        onStoreNameChange = viewModel::onStoreNameChange,
                        onPickExistingStore = { viewModel.onStoreSheetVisibleChange(true) },
                        onOpenMap = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:0,0?q=${Uri.encode(uiState.storeName.ifBlank { "商店" })}")
                            )
                            try {
                                mapPickerLauncher.launch(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "未找到可用于选点的地图应用", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onLocationInputChange = viewModel::onStoreLocationInputChange
                    )
                }
            }
            uiState.validationErrors["general"]?.let { error ->
                item {
                    ErrorMessageCard(error)
                }
            }
        }
    }
}

@Composable
private fun GoodsAndPriceCard(
    uiState: ManualEntryUiState,
    viewModel: ManualEntryViewModel
) {
    SectionCard(title = "买了什么") {
        OutlinedTextField(
            value = uiState.goodsName,
            onValueChange = viewModel::onGoodsNameChange,
            label = { Text("商品名称") },
            placeholder = { Text("例如：伊利纯牛奶 250ml") },
            isError = uiState.goodsNameError != null,
            supportingText = uiState.goodsNameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.price,
            onValueChange = viewModel::onPriceChange,
            label = { Text("成交价格") },
            isError = uiState.priceError != null,
            supportingText = uiState.priceError?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            prefix = { Text("¥", fontWeight = FontWeight.Bold) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CategoryAndStoreCard(
    uiState: ManualEntryUiState,
    viewModel: ManualEntryViewModel
) {
    SectionCard(title = "归到哪里") {
        SelectionCard(
            label = "分类",
            value = uiState.category.ifBlank { "选择或创建分类" },
            placeholder = uiState.category.isBlank(),
            onClick = { viewModel.onCategorySheetVisibleChange(true) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SelectionCard(
            label = "商店",
            value = uiState.storeName.ifBlank { "选择已有商店或新增" },
            placeholder = uiState.storeName.isBlank(),
            supportingText = uiState.storeAddress.ifBlank { null },
            isError = uiState.storeNameError != null,
            errorText = uiState.storeNameError,
            onClick = { viewModel.onStoreSheetVisibleChange(true) }
        )
    }
}

@Composable
private fun NewStoreLocationCard(
    uiState: ManualEntryUiState,
    onStoreNameChange: (String) -> Unit,
    onPickExistingStore: () -> Unit,
    onOpenMap: () -> Unit,
    onLocationInputChange: (String) -> Unit
) {
    SectionCard(title = "新增商店") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("商店信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onPickExistingStore) {
                Text("换已有")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = uiState.storeName,
            onValueChange = onStoreNameChange,
            label = { Text("商店名称") },
            isError = uiState.storeNameError != null,
            supportingText = uiState.storeNameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        FilledTonalButton(
            onClick = onOpenMap,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("打开地图复制分享链接")
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = uiState.storeLocationInput,
            onValueChange = onLocationInputChange,
            label = { Text("地图位置分享链接") },
            placeholder = { Text("粘贴高德地图分享链接") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        uiState.storeLocationError?.let { error ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        StoreLocationStatus(uiState)
    }
}

@Composable
private fun StoreLocationStatus(uiState: ManualEntryUiState) {
    when {
        uiState.storeLatitude != null && uiState.storeLongitude != null -> {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "已保存位置：${uiState.storeAddress.ifBlank { "${uiState.storeLatitude}, ${uiState.storeLongitude}" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        !uiState.storeMapUrl.isNullOrBlank() -> {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "未解析出经纬度，已保存原始地图链接；后续导航会直接打开该链接。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ManualEntryBottomBar(
    isSaving: Boolean,
    onSaveAndContinue: () -> Unit,
    onSaveOnly: () -> Unit
) {
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = onSaveAndContinue,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) { Text("保存并继续") }
            Button(
                onClick = onSaveOnly,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) { Text("仅保存") }
        }
    }
}

@Composable
private fun ErrorMessageCard(error: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(
            error,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun SelectionCard(
    label: String,
    value: String,
    placeholder: Boolean,
    onClick: () -> Unit,
    supportingText: String? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
            color = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (placeholder) FontWeight.Normal else FontWeight.SemiBold,
                        color = if (placeholder) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    supportingText?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
        errorText?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    query: String,
    categories: List<String>,
    canCreateCategory: Boolean,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onCreateCategory: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        PickerSheetHeader(title = "选择分类")
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("搜索或输入新分类") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
            if (categories.isEmpty()) {
                item {
                    EmptySheetHint("没有匹配分类")
                }
            } else {
                items(categories, key = { it }) { category ->
                    ListItem(
                        headlineContent = { Text(category, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.clickable { onCategorySelected(category) }
                    )
                    HorizontalDivider()
                }
            }
            if (canCreateCategory) {
                item {
                    ListItem(
                        headlineContent = { Text("创建「${query.trim()}」") },
                        leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                        modifier = Modifier.clickable(onClick = onCreateCategory)
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorePickerSheet(
    query: String,
    stores: List<com.pricekeeper.app.domain.model.Store>,
    onQueryChange: (String) -> Unit,
    onStoreSelected: (com.pricekeeper.app.domain.model.Store) -> Unit,
    onNewStore: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        PickerSheetHeader(title = "选择商店")
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("搜索商店名、区域或地址") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
            item {
                ListItem(
                    headlineContent = { Text("新增商店") },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onNewStore)
                )
                HorizontalDivider()
            }
            if (stores.isEmpty()) {
                item {
                    EmptySheetHint("没有匹配商店")
                }
            } else {
                items(stores, key = { it.id }) { store ->
                    ListItem(
                        headlineContent = { Text(store.name, fontWeight = FontWeight.Medium) },
                        supportingContent = {
                            Text(
                                listOf(store.region, store.address.orEmpty())
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · ")
                                    .ifBlank { "暂无地址信息" },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingContent = { Icon(Icons.Default.Store, contentDescription = null) },
                        modifier = Modifier.clickable { onStoreSelected(store) }
                    )
                    HorizontalDivider()
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PickerSheetHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptySheetHint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    )
}
