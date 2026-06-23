package com.pricekeeper.app.feature.receipt

import java.util.UUID

/**
 * A single editable line item on the receipt recognize/edit screen.
 */
data class EditableReceiptItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val price: String = "",
    val category: String = "未分类",
    val isEdited: Boolean = false
)
