package com.pricekeeper.app.feature.store

import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.pricekeeper.app.domain.model.StoreGoodsItem
import com.pricekeeper.app.feature.navigation.MapDestination
import com.pricekeeper.app.feature.navigation.openMapRoutePlan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    onNavigateBack: () -> Unit,
    onGoodsClick: (Long) -> Unit,
    viewModel: StoreDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showReviewEditor by remember { mutableStateOf(false) }

    if (showReviewEditor && uiState is StoreDetailUiState.Success) {
        StoreReviewEditorSheet(
            state = uiState as StoreDetailUiState.Success,
            viewModel = viewModel,
            onDismiss = { showReviewEditor = false },
            onSave = {
                viewModel.saveReview()
                showReviewEditor = false
            }
        )
    }

    Scaffold(
        topBar = {
            PriceKeeperTopBar(
                title = when (val s = uiState) {
                    is StoreDetailUiState.Success -> s.store.name
                    else -> "商店详情"
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is StoreDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is StoreDetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is StoreDetailUiState.Success -> {
                StoreDetailContent(
                    state = state,
                    onEditReview = { showReviewEditor = true },
                    onGoodsClick = onGoodsClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun StoreDetailContent(
    state: StoreDetailUiState.Success,
    onEditReview: () -> Unit,
    onGoodsClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        item {
            StoreLocationCard(
                state = state,
                onNavigate = {
                    openMapRoutePlan(
                        context,
                        MapDestination(
                            name = state.store.name,
                            address = state.store.address,
                            latitude = state.store.latitude,
                            longitude = state.store.longitude,
                            mapUrl = state.store.mapUrl
                        )
                    )
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            StoreReviewCard(state = state, onEditReview = onEditReview)
            Spacer(Modifier.height(20.dp))
        }

        item {
            GoodsSectionHeader(count = state.goodsSummaries.size)
            Spacer(Modifier.height(10.dp))
        }

        if (state.goodsSummaries.isEmpty()) {
            item {
                Text(
                    "该商店暂无商品记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            items(state.goodsSummaries, key = { it.goodsId }) { item ->
                StoreGoodsItemCard(
                    item = item,
                    currencyFormat = currencyFormat,
                    dateFormat = dateFormat,
                    onClick = { onGoodsClick(item.goodsId) }
                )
            }
        }
    }
}

@Composable
private fun StoreLocationCard(
    state: StoreDetailUiState.Success,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("商店位置", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    state.store.address ?: state.store.region.ifBlank { "暂无详细地址" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }
            Button(onClick = onNavigate) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("路线")
            }
        }
    }
}

@Composable
private fun StoreReviewCard(
    state: StoreDetailUiState.Success,
    onEditReview: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEditReview)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("我的评价", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    state.store.myNote?.takeIf { it.isNotBlank() } ?: "还没有评价",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(onClick = onEditReview) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("编辑")
            }
        }
    }
}

@Composable
private fun GoodsSectionHeader(count: Int) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("商品追踪", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "共 $count 件商品，按历史价格汇总",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StoreGoodsItemCard(
    item: StoreGoodsItem,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val hasWave = item.maxPrice > item.minPrice
    val minPriceText = currencyFormat.format(item.minPrice)
    val maxPriceText = currencyFormat.format(item.maxPrice)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.goodsName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.size(5.dp))
                        Text(
                            item.lastBuyDate?.let { "最近购买 ${dateFormat.format(Date(it))}" } ?: "暂无购买时间",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                PriceRangePill(
                    minPriceText = minPriceText,
                    maxPriceText = maxPriceText,
                    isSinglePrice = !hasWave
                )
            }
        }
    }
}

@Composable
private fun PriceRangePill(
    minPriceText: String,
    maxPriceText: String,
    isSinglePrice: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        shape = RoundedCornerShape(18.dp)
    ) {
        if (isSinglePrice) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    minPriceText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("最低价", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(minPriceText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("最高价", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    Text(maxPriceText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreReviewEditorSheet(
    state: StoreDetailUiState.Success,
    viewModel: StoreDetailViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("编辑我的评价", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "这段内容只在本机保存，可随时修改。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.reviewDraft,
                onValueChange = viewModel::onReviewChange,
                placeholder = { Text("例如：停车方便，晚饭时段人多；牛奶价格经常比附近便宜。") },
                minLines = 5,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("取消")
                }
                Button(
                    onClick = onSave,
                    enabled = !state.isSavingReview,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isSavingReview) "保存中" else "保存评价")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
