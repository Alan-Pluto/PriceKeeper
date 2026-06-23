package com.pricekeeper.app.feature.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pricekeeper.app.domain.model.PricePoint

@Composable
fun PriceTrendChart(
    points: List<PricePoint>,
    modifier: Modifier = Modifier.fillMaxWidth().height(200.dp)
) {
    if (points.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(
                "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }

    val gridColor = ChartColors.gridColor()
    val labelColor = ChartColors.labelColor()
    val lineColor = ChartColors.lineColor()
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val paddingLeft = 50f
        val paddingRight = 16f
        val paddingTop = 16f
        val paddingBottom = 28f

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val minPrice = points.minOf { it.price } * 0.95
        val maxPrice = points.maxOf { it.price } * 1.05
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)

        // Grid lines & Y labels
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = paddingTop + chartHeight * (1 - i.toFloat() / gridLines)
            val price = minPrice + priceRange * (i.toFloat() / gridLines)

            drawLine(gridColor, Offset(paddingLeft, y), Offset(size.width - paddingRight, y))
            val label = formatPriceLabel(price)
            drawText(textMeasurer, label, Offset(paddingLeft - textMeasurer.measure(label, labelStyle).size.width - 4f, y - 6f), labelStyle)
        }

        // X labels (first/last)
        if (points.isNotEmpty()) {
            val firstLabel = formatDateLabel(points.first().timestamp)
            val lastLabel = formatDateLabel(points.last().timestamp)
            drawText(textMeasurer, firstLabel, Offset(paddingLeft, size.height - paddingBottom + 4f), labelStyle)
            val lastW = textMeasurer.measure(lastLabel, labelStyle).size.width
            drawText(textMeasurer, lastLabel, Offset(size.width - paddingRight - lastW, size.height - paddingBottom + 4f), labelStyle)
        }

        // Price line
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = paddingLeft + chartWidth * index / (points.size - 1).coerceAtLeast(1)
            val y = paddingTop + chartHeight * (1 - ((point.price - minPrice) / priceRange).toFloat())
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Dots on data points
        points.forEachIndexed { index, point ->
            val x = paddingLeft + chartWidth * index / (points.size - 1).coerceAtLeast(1)
            val y = paddingTop + chartHeight * (1 - ((point.price - minPrice) / priceRange).toFloat())
            drawCircle(lineColor, 4.dp.toPx(), Offset(x, y))
        }
    }
}
