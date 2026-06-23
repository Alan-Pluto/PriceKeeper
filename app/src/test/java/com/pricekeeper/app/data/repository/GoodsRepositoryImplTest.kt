package com.pricekeeper.app.data.repository

import com.pricekeeper.app.data.local.dao.GoodsDao
import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.entity.GoodsEntity
import com.pricekeeper.app.domain.model.Goods
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GoodsRepositoryImplTest {

    private val goodsDao: GoodsDao = mockk()
    private val priceRecordDao: PriceRecordDao = mockk()
    private val repository = GoodsRepositoryImpl(goodsDao, priceRecordDao)

    @Test
    fun `observeGoods maps entities to domain models`() = runTest {
        val entity = GoodsEntity(
            id = 1, name = "牛奶", category = "饮料",
            specUnit = "1L", createdAt = 1000, updatedAt = 2000
        )
        every { goodsDao.observeAll() } returns flowOf(listOf(entity))

        val result = repository.observeGoods().toList(mutableListOf())

        assertEquals(1, result.size)
        val goods = result.first().first()
        assertEquals(1L, goods.id)
        assertEquals("牛奶", goods.name)
        assertEquals("饮料", goods.category)
        assertEquals("1L", goods.specUnit)
    }

    @Test
    fun `observeGoodsMinPrices maps live price aggregates`() = runTest {
        every { priceRecordDao.observeAllGoodsMinPrices() } returns flowOf(
            listOf(
                com.pricekeeper.app.data.local.dao.GoodsMinPrice(goodsId = 1L, minPrice = 9.9),
                com.pricekeeper.app.data.local.dao.GoodsMinPrice(goodsId = 2L, minPrice = 12.5)
            )
        )

        val result = repository.observeGoodsMinPrices().toList(mutableListOf())

        assertEquals(mapOf(1L to 9.9, 2L to 12.5), result.first())
    }

    @Test
    fun `getGoodsDetail returns null for unknown id`() = runTest {
        coEvery { goodsDao.getById(999L) } returns null

        val result = repository.getGoodsDetail(999L)

        assertNull(result)
    }

    @Test
    fun `createGoods inserts and returns id`() = runTest {
        coEvery { goodsDao.insert(any()) } returns 42L

        val id = repository.createGoods("面包", "食品", "500g")

        assertEquals(42L, id)
        coVerify { goodsDao.insert(any<GoodsEntity>()) }
    }

    @Test
    fun `createGoods returns existing id on conflict`() = runTest {
        coEvery { goodsDao.insert(any()) } returns -1L // IGNORE conflict
        coEvery { goodsDao.getByName("牛奶") } returns GoodsEntity(
            id = 7, name = "牛奶", category = "饮料"
        )

        val id = repository.createGoods("牛奶", "饮料")

        assertEquals(7L, id)
    }

    @Test
    fun `searchGoods maps results to domain`() = runTest {
        every { goodsDao.searchByName("牛") } returns flowOf(emptyList())

        val results = repository.searchGoods("牛").toList(mutableListOf())

        assertEquals(1, results.size)
        assert(results.first().isEmpty())
    }

    @Test
    fun `getGoodsById returns domain model`() = runTest {
        coEvery { goodsDao.getById(1L) } returns GoodsEntity(
            id = 1, name = "测试", category = "测试分类"
        )

        val goods = repository.getGoodsById(1L)

        assertNotNull(goods)
        assertEquals("测试", goods!!.name)
    }
}
