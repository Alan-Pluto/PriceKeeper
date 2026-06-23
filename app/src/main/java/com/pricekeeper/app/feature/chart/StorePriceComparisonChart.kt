package com.pricekeeper.app.feature.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pricekeeper.app.domain.model.StorePriceInfo

@Composable
fun StorePriceComparisonChart(
    storePrices: List<StorePriceInfo>,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    if (storePrices.isEmpty()) return

    val cheapestColor = ChartColors.cheapestColor()
    val expensiveColor = ChartColors.expensiveColor()
    val labelColor = ChartColors.labelColor()
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)
    val maxPrice = storePrices.maxOf { it.price }
    val barHeight = 28.dp

    val chartHeight = (barHeight * storePrices.size) + 8.dp * (storePrices.size - 1)

    Canvas(modifier = modifier.height(chartHeight).padding(start = 80.dp)) {
        val barAreaWidth = size.width - 16f
        val barThickness = barHeight.toPx()

        storePrices.forEachIndexed { index, info ->
            val y = index * (barThickness + 8.dp.toPx())
            val barWidth = ((info.price / maxPrice) * barAreaWidth).toFloat().coerceAtLeast(4.dp.toPx())
            val color = if (index == 0) cheapestColor else if (index == storePrices.size - 1) expensiveColor else labelColor

            // Bar
            drawRoundRect(
                color = color.copy(alpha = 0.8f),
                topLeft = Offset(0f, y),
                size = Size(barWidth, barThickness),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Price label
            val priceLabel = "¥%.2f".format(info.price)
            drawText(textMeasurer, priceLabel, Offset(barWidth + 4.dp.toPx(), y + barThickness / 2 - 7.dp.toPx()), labelStyle)

            // Store name (drawn on the left side by parent padding)
        }
    }
}
