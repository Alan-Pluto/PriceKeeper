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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.pricekeeper.app.domain.model.StorePriceInfo
import com.pricekeeper.app.feature.chart.PriceTrendChart
import com.pricekeeper.app.feature.navigation.MapDestination
import com.pricekeeper.app.feature.navigation.openMapRoutePlan
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
internal fun GoodsDetailContent(
    data: com.pricekeeper.app.domain.model.GoodsPriceDetail,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
    ) {
        // Price summary card
        item {
            PriceSummaryCard(
                lowestPrice = data.lowestPrice,
                highestPrice = data.highestPrice,
                latestPrice = data.latestPrice
            )
        }

        // Price trend chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("价格趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    PriceTrendChart(points = data.trend)
                }
            }
        }

        // Store comparison header
        item {
            Text(
                text = "比价列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        text = storePrice.storeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        dateFormat.format(Date(storePrice.recordDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    currencyFormat.format(storePrice.price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!storePrice.storeAddress.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    storePrice.storeAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    openMapRoutePlan(
                        context,
                        MapDestination(
                            name = storePrice.storeName,
                            address = storePrice.storeAddress,
                            latitude = storePrice.storeLatitude,
                            longitude = storePrice.storeLongitude,
                            mapUrl = storePrice.storeMapUrl
                        )
                    )
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("路线")
            }
        }
    }
}
