package com.pricekeeper.app.data.parser

import com.pricekeeper.app.domain.model.ParsedReceiptItem
import com.pricekeeper.app.domain.model.ReceiptParseResult
import javax.inject.Inject

/**
 * Parser for Yonghui (永辉商店) receipts.
 *
 * Yonghui receipts typically have a columnar format:
 *   品名    数量  单价  金额
 *   牛奶    1    12.5  12.5
 * Key detection keyword: "永辉"
 */
class YonghuiParser @Inject constructor() : ReceiptParser {

    companion object {
        const val DETECT_KEYWORD = "永辉"

        // Matches columnar format: 品名 数量 单价 金额
        // Line example: "蒙牛纯牛奶250ml  1  12.50  12.50"
        private val COLUMN_REGEX = Regex(
            """^(.+?)\s+(\d+(?:\.\d+)?)\s+(\d+\.?\d*)\s+(\d+\.?\d*)\s*$"""
        )

        private val TOTAL_KEYWORDS = listOf("合计", "总计", "应付", "实付", "找零")

        private val PRICE_REGEX = Regex("""(\d+\.?\d{1,2})\s*$""")
    }

    override fun parse(rawText: String): ReceiptParseResult {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val items = mutableListOf<ParsedReceiptItem>()
        var totalPrice: Double? = null

        for (line in lines) {
            // Skip header lines
            if (line.contains("品名") || line.contains("单价") || line.contains("数量") ||
                line.contains("---") || line.contains("===") || line.contains("商品") && line.contains("金额")
            ) continue

            // Check for total line
            if (TOTAL_KEYWORDS.any { line.contains(it) }) {
                val match = PRICE_REGEX.find(line)
                totalPrice = match?.groupValues?.get(1)?.toDoubleOrNull()
                continue
            }

            // Try columnar format
            val colMatch = COLUMN_REGEX.find(line)
            if (colMatch != null) {
                val name = colMatch.groupValues[1].trim()
                // Price is the 4th column (金额)
                val price = colMatch.groupValues[4].toDoubleOrNull()
                if (name.isNotEmpty() && price != null && price > 0 && name.length < 30) {
                    items.add(ParsedReceiptItem(name = name, price = price))
                }
                continue
            }

            // Fallback: generic price extraction
            val priceMatch = PRICE_REGEX.find(line)
            if (priceMatch != null) {
                val price = priceMatch.groupValues[1].toDoubleOrNull()
                val name = line.substring(0, priceMatch.range.first).trim()
                if (name.isNotEmpty() && name.length < 30 && price != null && price > 0) {
                    items.add(ParsedReceiptItem(name = name, price = price))
                }
            }
        }

        return ReceiptParseResult(
            items = items,
            totalPrice = totalPrice,
            storeNameHint = "永辉商店",
            parserName = "YonghuiParser"
        )
    }
}
