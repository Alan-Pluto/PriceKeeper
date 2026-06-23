package com.pricekeeper.app.feature.profile

import com.pricekeeper.app.core.ui.theme.PriceKeeperTopBar
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.pricekeeper.app.data.export.ImportConflictStrategy
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToCategory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/x-pricekeeper-data")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it, ImportConflictStrategy.MERGE) }
    }

    // Show toast on export/import result
    uiState.exportResult?.let { stats ->
        Toast.makeText(context, "导出成功：${stats.goodsCount}件商品", Toast.LENGTH_SHORT).show()
        viewModel.clearExportResult()
    }
    uiState.importResult?.let { result ->
        if (result.isSuccess) {
            Toast.makeText(context, "导入成功：${result.totalImported}条记录", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "导入失败：${result.errors.firstOrNull()}", Toast.LENGTH_LONG).show()
        }
        viewModel.clearImportResult()
    }

    Scaffold(
        topBar = {
            PriceKeeperTopBar(
                title = "我的",
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Dashboard cards
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                DashboardCard(
                    title = "商品",
                    value = "${uiState.dashboardStats.goodsCount}件",
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                DashboardCard(
                    title = "商店",
                    value = "${uiState.dashboardStats.storeCount}家",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                DashboardCard(
                    title = "总消费",
                    value = currencyFormat.format(uiState.dashboardStats.totalSpending),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                DashboardCard(
                    title = "小票",
                    value = "${uiState.dashboardStats.receiptCount}张",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Data sovereignty
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { exportLauncher.launch("PriceKeeper_${System.currentTimeMillis()}.mypd") },
                    enabled = !uiState.isExporting,
                    modifier = Modifier.weight(1f)
                ) { Text(if (uiState.isExporting) "导出中..." else "📤 导出数据") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    enabled = !uiState.isImporting,
                    modifier = Modifier.weight(1f)
                ) { Text(if (uiState.isImporting) "导入中..." else "📥 导入数据") }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Category management
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCategory() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("默认分类管理", modifier = Modifier.weight(1f))
                Text(">", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            // About
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAbout() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("关于", modifier = Modifier.weight(1f))
                Text(">", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
        }
    }
}
