package com.pricekeeper.app.data.parser

/**
 * Unrecoverable error during receipt parsing.
 */
class ReceiptParserException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
