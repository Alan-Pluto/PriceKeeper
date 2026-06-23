package com.pricekeeper.app.feature.goods

import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.pricekeeper.app.domain.model.StorePriceInfo
import com.pricekeeper.app.feature.chart.PriceTrendChart
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoodsDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoodsDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PriceKeeperTopBar(
                title = when (val state = uiState) {
                    is GoodsDetailUiState.Success -> state.data.goods.name
                    else -> "商品详情"
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
            is GoodsDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is GoodsDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is GoodsDetailUiState.Success -> {
                GoodsDetailContent(
                    data = state.data,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun GoodsDetailContent(
    data: com.pricekeeper.app.domain.model.GoodsPriceDetail,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Price summary card
        item {
            PriceSummaryCard(
                lowestPrice = data.lowestPrice,
                highestPrice = data.highestPrice,
                latestPrice = data.latestPrice
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Price trend chart
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("价格趋势", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    PriceTrendChart(points = data.trend)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Store comparison header
        item {
            Text(
                text = "比价列表",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Store price list
        if (data.storePrices.isEmpty()) {
            item {
                Text(
                    text = "暂无商店比价数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(data.storePrices, key = { it.storeName + it.recordDate }) { storePrice ->
                StorePriceItem(
                    storePrice = storePrice,
                    dateFormat = dateFormat,
                    currencyFormat = currencyFormat
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun StorePriceItem(
    storePrice: StorePriceInfo,
    dateFormat: SimpleDateFormat,
    currencyFormat: NumberFormat
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = storePrice.storeName, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${currencyFormat.format(storePrice.price)} · ${dateFormat.format(Date(storePrice.recordDate))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = {
                val uri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(storePrice.storeName)}")
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                try { context.startActivity(intent) } catch (_: Exception) {
                    try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri)) } catch (_: Exception) {}
                }
            }) {
                Text("🗺️", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
