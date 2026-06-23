package com.pricekeeper.app.data.parser

import com.pricekeeper.app.domain.model.ParsedReceiptItem
import com.pricekeeper.app.domain.model.ReceiptParseResult
import javax.inject.Inject

/**
 * Default receipt parser using regex-based line extraction.
 * Three-pass approach:
 * 1. Detect total price from keywords (合计/总计/应付/实付)
 * 2. Extract line items via "品名 ¥价格" pattern
 * 3. Attempt store name from header lines
 */
class DefaultParser @Inject constructor() : ReceiptParser {

    companion object {
        // Matches: 商品名 ¥12.5 or 商品名 12.5 or 商品名 ¥12.5  （空格分隔）
        private val ITEM_REGEX = Regex(
            """^(.+?)\s*[¥￥]\s*(\d+\.?\d*)\s*$""",
            RegexOption.MULTILINE
        )

        // Fallback: matches lines where price appears near the end
        private val PRICE_REGEX = Regex("""(\d+\.?\d{1,2})\s*$""")

        // Total price keywords
        private val TOTAL_KEYWORDS = listOf("合计", "总计", "应付", "实付", "总价", "应收")

        // Common store name indicators (appear in header)
        private val STORE_KEYWORDS = listOf("超市", "商场", "商店", "便利店", "永辉", "盒马", "沃尔玛", "物美")

        private const val HEADER_LINES = 4
    }

    override fun parse(rawText: String): ReceiptParseResult {
        if (rawText.isBlank()) {
            return ReceiptParseResult(
                items = emptyList(),
                totalPrice = null,
                storeNameHint = null,
                parserName = "DefaultParser"
            )
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // Pass 1: Detect total price
        val totalPrice = detectTotalPrice(lines)

        // Pass 2: Extract line items
        val items = extractItems(lines)

        // Pass 3: Detect store name from header
        val storeNameHint = detectStoreName(lines.take(HEADER_LINES))

        return ReceiptParseResult(
            items = items,
            totalPrice = totalPrice,
            storeNameHint = storeNameHint,
            parserName = "DefaultParser"
        )
    }

    private fun detectTotalPrice(lines: List<String>): Double? {
        for (line in lines) {
            for (keyword in TOTAL_KEYWORDS) {
                if (line.contains(keyword)) {
                    val match = PRICE_REGEX.find(line)
                    val price = match?.groupValues?.get(1)?.toDoubleOrNull()
                    if (price != null && price > 0) return price
                }
            }
        }
        return null
    }

    private fun extractItems(lines: List<String>): List<ParsedReceiptItem> {
        return lines.mapNotNull { line ->
            val match = ITEM_REGEX.find(line)
            if (match != null) {
                val name = match.groupValues[1].trim()
                val price = match.groupValues[2].toDoubleOrNull()
                if (name.isNotEmpty() && price != null && price > 0) {
                    ParsedReceiptItem(name = name, price = price)
                } else null
            } else {
                // Fallback: try generic price extraction
                val priceMatch = PRICE_REGEX.find(line)
                if (priceMatch != null) {
                    val price = priceMatch.groupValues[1].toDoubleOrNull()
                    val name = line.substring(0, priceMatch.range.first).trim()
                    if (name.isNotEmpty() && name.length < 30 && price != null && price > 0) {
                        // Avoid matching total price lines as items
                        val isTotalLine = TOTAL_KEYWORDS.any { line.contains(it) }
                        if (!isTotalLine) {
                            ParsedReceiptItem(name = name, price = price)
                        } else null
                    } else null
                } else null
            }
        }
    }

    private fun detectStoreName(headerLines: List<String>): String? {
        for (line in headerLines) {
            for (keyword in STORE_KEYWORDS) {
                if (line.contains(keyword)) {
                    // Return the store name part (up to 20 chars around the keyword)
                    val index = line.indexOf(keyword)
                    val start = maxOf(0, index - 4)
                    val end = minOf(line.length, index + keyword.length + 6)
                    return line.substring(start, end).trim().ifEmpty { null }
                }
            }
        }
        return null
    }
}
