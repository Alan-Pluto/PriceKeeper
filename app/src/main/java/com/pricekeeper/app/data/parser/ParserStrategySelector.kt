package com.pricekeeper.app.data.parser

import com.pricekeeper.app.domain.model.ReceiptParseResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Strategy selector: scans raw OCR text for store keywords and delegates
 * to the appropriate parser. Falls back to DefaultParser when no match.
 */
@Singleton
class ParserStrategySelector @Inject constructor(
    private val defaultParser: DefaultParser,
    private val yonghuiParser: YonghuiParser,
    private val hemaParser: HemaParser
) : ReceiptParser {

    /**
     * Ordered list of keyword→parser mappings. Earlier entries take priority.
     */
    private val strategyChain = listOf(
        YonghuiParser.DETECT_KEYWORD to yonghuiParser,
        HemaParser.DETECT_KEYWORD to hemaParser
    )

    override fun parse(rawText: String): ReceiptParseResult {
        // Scan first 500 chars for store keywords (store name always near top)
        val header = rawText.take(500)

        for ((keyword, parser) in strategyChain) {
            if (header.contains(keyword, ignoreCase = true)) {
                return parser.parse(rawText)
            }
        }

        return defaultParser.parse(rawText)
    }
}
