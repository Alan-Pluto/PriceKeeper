package com.pricekeeper.app.feature.manual

import com.pricekeeper.app.domain.repository.CategoryRepository
import com.pricekeeper.app.domain.repository.GoodsRepository
import com.pricekeeper.app.domain.repository.StoreRepository
import com.pricekeeper.app.domain.usecase.AddManualPriceRecordUseCase
import com.pricekeeper.app.core.location.StoreLocationInfo
import com.pricekeeper.app.helpers.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ManualEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase: AddManualPriceRecordUseCase = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val goodsRepository: GoodsRepository = mockk()
    private val storeRepository: StoreRepository = mockk()
    private lateinit var viewModel: ManualEntryViewModel

    private fun createViewModel() {
        every { categoryRepository.observeCategories() } returns flowOf(emptyList())
        every { goodsRepository.observeAllCategories() } returns flowOf(emptyList())
        every { storeRepository.observeStores() } returns flowOf(emptyList())
        viewModel = ManualEntryViewModel(useCase, categoryRepository, goodsRepository, storeRepository)
    }

    @Test
    fun `initial state has empty fields`() = runTest {
        createViewModel()
        val state = viewModel.uiState.first()
        assertEquals("", state.goodsName)
        assertEquals("", state.price)
        assertEquals("", state.storeName)
        assertTrue(state.validationErrors.isEmpty())
    }

    @Test
    fun `save triggers validation for empty fields`() = runTest {
        createViewModel()
        viewModel.saveOnly()
        val state = viewModel.uiState.first()
        assertNotNull(state.goodsNameError)
        assertNotNull(state.priceError)
        assertNotNull(state.storeNameError)
    }

    @Test
    fun `onPriceChange rejects invalid price strings`() = runTest {
        createViewModel()
        viewModel.onPriceChange("12.5")
        assertEquals("12.5", viewModel.uiState.first().price)
        viewModel.onPriceChange("12.5a")
        assertEquals("12.5", viewModel.uiState.first().price)
    }

    @Test
    fun `onGoodsNameChange clears name error`() = runTest {
        createViewModel()
        viewModel.saveOnly()
        assertNotNull(viewModel.uiState.first().goodsNameError)
        viewModel.onGoodsNameChange("牛奶")
        assertNull(viewModel.uiState.first().goodsNameError)
    }

    @Test
    fun `saveAndContinue resets fields on success`() = runTest {
        coEvery { categoryRepository.addCategory(any()) } returns Unit
        coEvery { useCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success(42L)
        createViewModel()
        viewModel.onGoodsNameChange("牛奶")
        viewModel.onPriceChange("12.5")
        viewModel.onStoreNameChange("永辉商店")

        viewModel.saveAndContinue()
        val state = viewModel.uiState.first()
        assertEquals("", state.goodsName)
        assertEquals("", state.price)
        assertTrue(state.saveSuccess)
    }

    @Test
    fun `saveOnly waits for success before requesting navigation back`() = runTest {
        coEvery { categoryRepository.addCategory(any()) } returns Unit
        coEvery { useCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success(42L)
        createViewModel()
        viewModel.onGoodsNameChange("牛奶")
        viewModel.onPriceChange("12.5")
        viewModel.onStoreNameChange("永辉商店")

        viewModel.saveOnly()

        val state = viewModel.uiState.first()
        assertTrue(state.saveSuccess)
        assertTrue(state.pendingBackAfterSave)
    }

    @Test
    fun `save parses map link and uses resolved store address and region`() = runTest {
        coEvery { categoryRepository.addCategory(any()) } returns Unit
        coEvery { useCase(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Result.success(42L)
        createViewModel()
        viewModel.onGoodsNameChange("牛奶")
        viewModel.onPriceChange("12.5")
        viewModel.onNewStoreToggle()
        viewModel.onStoreNameChange("永辉商店")
        val mapUrl = "https://www.amap.com/?p=B0TEST,31.2304,121.4737,store,address"
        viewModel.onStoreLocationInputChange(mapUrl)

        viewModel.saveOnly { latitude, longitude ->
            StoreLocationInfo(
                latitude = latitude,
                longitude = longitude,
                region = "上海市黄浦区",
                address = "上海市黄浦区人民大道",
                mapUrl = mapUrl
            )
        }

        coVerify {
            useCase(
                goodsName = "牛奶",
                storeName = "永辉商店",
                price = 12.5,
                recordDate = any(),
                goodsCategory = "未分类",
                storeRegion = "上海市黄浦区",
                storeAddress = "上海市黄浦区人民大道",
                storeLatitude = 31.2304,
                storeLongitude = 121.4737,
                storeMapUrl = mapUrl,
                isPromotion = false,
                note = null
            )
        }
    }

    @Test
    fun `location link only result stores map url`() = runTest {
        createViewModel()

        viewModel.onStoreLocationLinkSaved("https://surl.amap.com/bGKm9Ho1b2us")

        val state = viewModel.uiState.first()
        assertEquals("https://surl.amap.com/bGKm9Ho1b2us", state.storeMapUrl)
        assertNull(state.storeLocationError)
    }

    @Test
    fun `location invalid result stores validation error`() = runTest {
        createViewModel()

        viewModel.onStoreLocationParseFailed("请粘贴以 http:// 或 https:// 开头的地图分享链接")

        val state = viewModel.uiState.first()
        assertEquals("请粘贴以 http:// 或 https:// 开头的地图分享链接", state.storeLocationError)
        assertNull(state.storeMapUrl)
    }
}
