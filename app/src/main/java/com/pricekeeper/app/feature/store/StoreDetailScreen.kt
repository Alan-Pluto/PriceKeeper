package com.pricekeeper.app.feature.store

import android.content.Intent
import android.net.Uri
import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.pricekeeper.app.domain.model.StoreGoodsItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: StoreDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                StoreDetailContent(state, viewModel, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun StoreDetailContent(
    state: StoreDetailUiState.Success,
    viewModel: StoreDetailViewModel,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        item {
            StoreLocationCard(state = state, onNavigate = { openStoreNavigation(context, state) })
            Spacer(Modifier.height(16.dp))
        }

        item {
            StoreReviewCard(state = state, viewModel = viewModel)
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text(
                "已追踪 ${state.goodsSummaries.size} 件商品",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
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
                StoreGoodsItemCard(item, currencyFormat, dateFormat)
                HorizontalDivider()
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
                Text("商店位置", style = MaterialTheme.typography.titleSmall)
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
                Text("导航")
            }
        }
    }
}

@Composable
private fun StoreReviewCard(
    state: StoreDetailUiState.Success,
    viewModel: StoreDetailViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("我的评价", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.reviewDraft,
                onValueChange = viewModel::onReviewChange,
                placeholder = { Text("写下这家店的体验、服务或价格印象") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (state.store.myNote.isNullOrBlank()) "暂无评价" else "已保存评价",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = viewModel::saveReview,
                    enabled = !state.isSavingReview
                ) {
                    Text(if (state.isSavingReview) "保存中" else "保存评价")
                }
            }
        }
    }
}

@Composable
private fun StoreGoodsItemCard(
    item: StoreGoodsItem,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    val hasWave = item.maxPrice > item.minPrice
    val waveText = if (hasWave) {
        "波动 ${currencyFormat.format(item.maxPrice - item.minPrice)}"
    } else {
        "价格稳定"
    }
    val waveColor = if (hasWave) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.goodsName, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    item.lastBuyDate?.let { "最近购买：${dateFormat.format(Date(it))}" } ?: "暂无购买时间",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "${currencyFormat.format(item.minPrice)} ~ ${currencyFormat.format(item.maxPrice)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    waveText,
                    style = MaterialTheme.typography.labelMedium,
                    color = waveColor,
                    modifier = Modifier
                        .background(waveColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun openStoreNavigation(
    context: android.content.Context,
    state: StoreDetailUiState.Success
) {
    val latitude = state.store.latitude
    val longitude = state.store.longitude
    if (latitude != null && longitude != null) {
        val amapIntent = Intent(Intent.ACTION_VIEW, buildAmapNavigationUri(latitude, longitude, state.store.name))
            .setPackage(AMAP_PACKAGE_NAME)
        try {
            context.startActivity(amapIntent)
            return
        } catch (_: Exception) {
            // 未安装高德或 URI 不被支持时，继续使用系统通用 geo 协议兜底。
        }
    }

    val fallbackUri = when {
        latitude != null && longitude != null ->
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(state.store.name)})")
        !state.store.mapUrl.isNullOrBlank() -> Uri.parse(state.store.mapUrl)
        else -> {
            val query = state.store.address ?: state.store.name
            Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        }
    }
    val intent = Intent(Intent.ACTION_VIEW, fallbackUri)
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        if (!state.store.mapUrl.isNullOrBlank() && fallbackUri.toString() != state.store.mapUrl) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.store.mapUrl)))
        }
    }
}

internal fun buildAmapNavigationUri(latitude: Double, longitude: Double, name: String): Uri {
    return Uri.parse(
        "androidamap://navi?sourceApplication=PriceKeeper" +
            "&lat=$latitude" +
            "&lon=$longitude" +
            "&poiname=${Uri.encode(name)}" +
            "&dev=0" +
            "&style=2"
    )
}

private const val AMAP_PACKAGE_NAME = "com.autonavi.minimap"
