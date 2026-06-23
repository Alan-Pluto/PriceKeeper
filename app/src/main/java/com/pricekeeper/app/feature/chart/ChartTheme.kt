package com.pricekeeper.app.feature.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

/** Chart color constants derived from MaterialTheme. */
object ChartColors {
    @Composable
    fun lineColor() = MaterialTheme.colorScheme.primary

    @Composable
    fun gridColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    @Composable
    fun labelColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    @Composable
    fun cheapestColor() = Color(0xFF2E7D32)

    @Composable
    fun expensiveColor() = MaterialTheme.colorScheme.error

    @Composable
    fun fillGradientStart() = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    @Composable
    fun fillGradientEnd() = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
}

/** Simple date axis label formatting. */
fun formatDateLabel(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val fmt = java.text.SimpleDateFormat("M/d", java.util.Locale.getDefault())
    return fmt.format(date)
}

/** Simple price axis label formatting. */
fun formatPriceLabel(price: Double): String {
    return if (price >= 1000) "%.0f".format(price)
    else if (price >= 10) "%.1f".format(price)
    else "%.2f".format(price)
}
