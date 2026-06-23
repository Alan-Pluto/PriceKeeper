package com.pricekeeper.app.feature.goods

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PriceSummaryCard(
    lowestPrice: Double?,
    highestPrice: Double?,
    latestPrice: Double?,
    modifier: Modifier = Modifier
) {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            PriceItem(
                label = "最低价",
                value = lowestPrice?.let { format.format(it) } ?: "--",
                color = MaterialTheme.colorScheme.primary
            )
            PriceItem(
                label = "最高价",
                value = highestPrice?.let { format.format(it) } ?: "--",
                color = MaterialTheme.colorScheme.error
            )
            PriceItem(
                label = "最新价",
                value = latestPrice?.let { format.format(it) } ?: "--",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun PriceItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
