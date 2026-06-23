package com.pricekeeper.app.data.parser

import com.pricekeeper.app.domain.model.ParsedReceiptItem
import com.pricekeeper.app.domain.model.ReceiptParseResult
import javax.inject.Inject

/**
 * Parser for Hema (盒马鲜生) receipts.
 *
 * Hema receipts have a distinct format:
 *   名称 ×数量  ¥价格
 *   蒙牛纯牛奶 ×1  ¥12.50
 * Total is labeled "实付" or "合计".
 * Key detection keyword: "盒马"
 */
class HemaParser @Inject constructor() : ReceiptParser {

    companion object {
        const val DETECT_KEYWORD = "盒马"

        // Matches: 商品名 ×数量 ¥价格
        private val ITEM_REGEX = Regex(
            """^(.+?)\s*[xX×]\s*(\d+)\s*[¥￥]\s*(\d+\.?\d*)\s*$"""
        )

        // Matches: 商品名 ¥价格 (without quantity)
        private val SIMPLE_REGEX = Regex(
            """^(.+?)\s*[¥￥]\s*(\d+\.?\d*)\s*$"""
        )

        private val TOTAL_KEYWORDS = listOf("实付", "合计", "总计", "应付")

        private val PRICE_REGEX = Regex("""[¥￥]\s*(\d+\.?\d{1,2})\s*$""")
    }

    override fun parse(rawText: String): ReceiptParseResult {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val items = mutableListOf<ParsedReceiptItem>()
        var totalPrice: Double? = null

        for (line in lines) {
            // Skip metadata lines
            if (line.contains("盒马") || line.contains("门店") || line.contains("订单") ||
                line.contains("会员") || line.contains("时间") || line.contains("---")
            ) continue

            // Check for total line (use ¥ prefix for better accuracy)
            if (TOTAL_KEYWORDS.any { line.contains(it) }) {
                val match = PRICE_REGEX.find(line)
                totalPrice = match?.groupValues?.get(1)?.toDoubleOrNull()
                continue
            }

            // Try "名称 ×数量 ¥价格" format
            val itemMatch = ITEM_REGEX.find(line)
            if (itemMatch != null) {
                val name = itemMatch.groupValues[1].trim()
                val price = itemMatch.groupValues[3].toDoubleOrNull()
                if (name.isNotEmpty() && price != null && price > 0 && name.length < 30) {
                    items.add(ParsedReceiptItem(name = name, price = price))
                }
                continue
            }

            // Try simple "名称 ¥价格" format
            val simpleMatch = SIMPLE_REGEX.find(line)
            if (simpleMatch != null) {
                val name = simpleMatch.groupValues[1].trim()
                val price = simpleMatch.groupValues[2].toDoubleOrNull()
                if (name.isNotEmpty() && price != null && price > 0 && name.length < 30) {
                    items.add(ParsedReceiptItem(name = name, price = price))
                }
            }
        }

        return ReceiptParseResult(
            items = items,
            totalPrice = totalPrice,
            storeNameHint = "盒马鲜生",
            parserName = "HemaParser"
        )
    }
}
