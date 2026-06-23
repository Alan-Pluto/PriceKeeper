package com.pricekeeper.app.data.di

import com.pricekeeper.app.data.repository.GoodsRepositoryImpl
import com.pricekeeper.app.data.repository.CategoryRepositoryImpl
import com.pricekeeper.app.data.repository.PriceRecordRepositoryImpl
import com.pricekeeper.app.data.repository.ReceiptRepositoryImpl
import com.pricekeeper.app.data.repository.StoreRepositoryImpl
import com.pricekeeper.app.domain.repository.CategoryRepository
import com.pricekeeper.app.domain.repository.GoodsRepository
import com.pricekeeper.app.domain.repository.PriceRecordRepository
import com.pricekeeper.app.domain.repository.ReceiptRepository
import com.pricekeeper.app.domain.repository.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGoodsRepository(impl: GoodsRepositoryImpl): GoodsRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindStoreRepository(impl: StoreRepositoryImpl): StoreRepository

    @Binds
    @Singleton
    abstract fun bindPriceRecordRepository(impl: PriceRecordRepositoryImpl): PriceRecordRepository

    @Binds
    @Singleton
    abstract fun bindReceiptRepository(impl: ReceiptRepositoryImpl): ReceiptRepository
}
