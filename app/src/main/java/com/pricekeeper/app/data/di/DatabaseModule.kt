package com.pricekeeper.app.data.di

import android.content.Context
import androidx.room.Room
import com.pricekeeper.app.data.local.dao.GoodsDao
import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.dao.ReceiptDao
import com.pricekeeper.app.data.local.dao.StoreDao
import com.pricekeeper.app.data.local.database.PriceKeeperDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PriceKeeperDatabase {
        return Room.databaseBuilder(
            context,
            PriceKeeperDatabase::class.java,
            PriceKeeperDatabase.DATABASE_NAME
        )
            .addMigrations(*PriceKeeperDatabase.MIGRATIONS)
            .build()
    }

    @Provides
    fun provideGoodsDao(database: PriceKeeperDatabase): GoodsDao {
        return database.goodsDao()
    }

    @Provides
    fun provideStoreDao(database: PriceKeeperDatabase): StoreDao {
        return database.storeDao()
    }

    @Provides
    fun providePriceRecordDao(database: PriceKeeperDatabase): PriceRecordDao {
        return database.priceRecordDao()
    }

    @Provides
    fun provideReceiptDao(database: PriceKeeperDatabase): ReceiptDao {
        return database.receiptDao()
    }
}
