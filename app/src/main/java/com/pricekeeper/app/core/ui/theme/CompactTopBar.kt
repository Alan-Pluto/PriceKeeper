package com.pricekeeper.app.core.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 全 App 共用的紧凑标题栏。
 *
 * 后续如果要调整标题栏高度、标题位置、标题字号，优先改这个文件；
 * 使用方页面只负责传入 title / navigationIcon，不要在各页面重复写 TopAppBar。
 */
@Composable
fun PriceKeeperTopBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    titleContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            // 标题栏总高度。想继续压窄可改成 44.dp；如果标题/图标显得挤，可改回 52.dp。
            .height(48.dp),
        color = containerColor,
        contentColor = titleContentColor
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 左侧导航图标区域，例如返回按钮。
            // 有 navigationIcon 时使用较小左边距，让图标视觉上贴近系统返回按钮习惯。
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .padding(start = if (navigationIcon == null) 16.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navigationIcon?.invoke()
            }

            // 标题位置控制点：
            // - Alignment.Center：水平/垂直居中，适合主页面标题。
            // - Alignment.CenterStart：靠左且垂直居中，适合偏工具型页面。
            // 不建议用 TopCenter/BottomCenter，容易在紧凑标题栏里显得偏移。
            Text(
                text = title,
                // 标题字号控制点：
                // - titleMedium：当前默认，清晰且不会太占高度。
                // - titleSmall：更紧凑。
                // - 也可以改成 MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp) 精确控制。
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    // 标题左右避让区：
                    // 有返回按钮时给左右各 56.dp，避免长标题压到返回按钮；
                    // 无返回按钮时只保留 16.dp 页面边距。
                    .padding(horizontal = if (navigationIcon == null) 16.dp else 56.dp)
            )
        }
    }
}
