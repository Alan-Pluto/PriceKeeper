package com.pricekeeper.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pricekeeper.app.feature.goods.GoodsDetailScreen
import com.pricekeeper.app.feature.goods.GoodsListScreen
import com.pricekeeper.app.feature.home.HomeScreen
import com.pricekeeper.app.feature.manual.ManualEntryScreen
import com.pricekeeper.app.feature.profile.AboutScreen
import com.pricekeeper.app.feature.profile.CategoryManagementScreen
import com.pricekeeper.app.feature.profile.ProfileScreen
import com.pricekeeper.app.feature.receipt.ReceiptCaptureScreen
import com.pricekeeper.app.feature.receipt.ReceiptRecognizeScreen
import com.pricekeeper.app.feature.store.StoreDetailScreen
import com.pricekeeper.app.feature.store.StoreScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Route.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home
        composable(Route.HOME) {
            HomeScreen(
                onNavigateToReceipt = {
                    navController.navigate(Route.RECEIPT_CAPTURE)
                },
                onNavigateToManual = {
                    navController.navigate(Route.MANUAL_ENTRY)
                }
            )
        }

        // Goods list
        composable(Route.GOODS) {
            GoodsListScreen(
                onGoodsClick = { goodsId ->
                    navController.navigate(Route.goodsDetail(goodsId))
                }
            )
        }

        // Goods detail
        composable(
            route = Route.GOODS_DETAIL,
            arguments = listOf(navArgument("goodsId") { type = NavType.LongType })
        ) {
            GoodsDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Store list
        composable(Route.STORE) {
            StoreScreen(
                onStoreClick = { storeId ->
                    navController.navigate(Route.storeDetail(storeId))
                }
            )
        }

        // Store detail
        composable(
            route = Route.STORE_DETAIL,
            arguments = listOf(navArgument("storeId") { type = NavType.LongType })
        ) {
            StoreDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Manual entry
        composable(Route.MANUAL_ENTRY) {
            ManualEntryScreen(
                onSaveAndBack = { navController.popBackStack() }
            )
        }

        // Receipt capture (Step 1)
        composable(Route.RECEIPT_CAPTURE) {
            ReceiptCaptureScreen(
                onImageReady = { imagePath ->
                    navController.navigate(Route.receiptRecognize(imagePath))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Receipt recognize (Step 2: OCR + edit)
        composable(
            route = Route.RECEIPT_RECOGNIZE,
            arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
        ) {
            ReceiptRecognizeScreen(
                onConfirmSave = { _, _, _ ->
                    navController.popBackStack(Route.HOME, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Profile
        composable(Route.PROFILE) {
            ProfileScreen(
                onNavigateToCategory = {
                    navController.navigate(Route.CATEGORY_MANAGEMENT)
                },
                onNavigateToAbout = {
                    navController.navigate(Route.ABOUT)
                }
            )
        }

        // About
        composable(Route.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        // Category management
        composable(Route.CATEGORY_MANAGEMENT) {
            CategoryManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
