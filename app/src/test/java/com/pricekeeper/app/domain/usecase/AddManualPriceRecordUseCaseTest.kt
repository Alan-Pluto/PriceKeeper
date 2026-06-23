package com.pricekeeper.app.domain.usecase

import com.pricekeeper.app.domain.repository.PriceRecordRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddManualPriceRecordUseCaseTest {

    private val repository: PriceRecordRepository = mockk()
    private val useCase = AddManualPriceRecordUseCase(repository)

    @Test
    fun `invoke with valid inputs returns success`() = runTest {
        coEvery {
            repository.addPriceRecord(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns 42L

        val result = useCase("牛奶", "永辉商店", 12.5)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
        coVerify(exactly = 1) {
            repository.addPriceRecord(
                goodsName = "牛奶",
                storeName = "永辉商店",
                price = 12.5,
                recordDate = any(),
                goodsCategory = "未分类",
                storeRegion = "",
                storeAddress = null,
                storeLatitude = null,
                storeLongitude = null,
                storeMapUrl = null,
                isPromotion = false,
                note = null,
                receiptId = null
            )
        }
    }

    @Test
    fun `invoke with blank goods name returns failure`() = runTest {
        val result = useCase("  ", "永辉商店", 10.0)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertEquals("商品名称不能为空", error?.message)
    }

    @Test
    fun `invoke with non-positive price returns failure`() = runTest {
        val result = useCase("牛奶", "永辉商店", 0.0)

        assertTrue(result.isFailure)
        assertEquals("价格必须大于0", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with blank store name returns failure`() = runTest {
        val result = useCase("牛奶", "", 10.0)

        assertTrue(result.isFailure)
        assertEquals("商店名称不能为空", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke trims whitespace from names`() = runTest {
        coEvery {
            repository.addPriceRecord(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns 1L

        val result = useCase("  牛奶  ", "  永辉商店  ", 10.0)

        assertTrue(result.isSuccess)
        coVerify {
            repository.addPriceRecord(
                goodsName = "牛奶",
                storeName = "永辉商店",
                price = any(),
                recordDate = any(),
                goodsCategory = any(),
                storeRegion = any(),
                storeAddress = any(),
                storeLatitude = any(),
                storeLongitude = any(),
                storeMapUrl = any(),
                isPromotion = any(),
                note = any(),
                receiptId = any()
            )
        }
    }
}
