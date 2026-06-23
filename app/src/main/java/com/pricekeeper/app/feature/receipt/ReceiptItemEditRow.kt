package com.pricekeeper.app.feature.receipt

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Editable row for a single receipt item. */
@Composable
fun ReceiptItemEditRow(
    item: EditableReceiptItem,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = item.name,
            onValueChange = onNameChange,
            label = { Text("商品") },
            singleLine = true,
            modifier = Modifier.weight(2f)
        )
        Spacer(Modifier.width(4.dp))
        OutlinedTextField(
            value = item.price,
            onValueChange = onPriceChange,
            label = { Text("价格") },
            singleLine = true,
            prefix = { Text("¥") },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, "删除", tint = MaterialTheme.colorScheme.error)
        }
    }
}
