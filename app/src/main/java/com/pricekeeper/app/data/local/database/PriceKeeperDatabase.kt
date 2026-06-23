package com.pricekeeper.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pricekeeper.app.data.local.dao.GoodsDao
import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.dao.ReceiptDao
import com.pricekeeper.app.data.local.dao.StoreDao
import com.pricekeeper.app.data.local.entity.GoodsEntity
import com.pricekeeper.app.data.local.entity.PriceRecordEntity
import com.pricekeeper.app.data.local.entity.ReceiptEntity
import com.pricekeeper.app.data.local.entity.StoreEntity

@Database(
    entities = [
        GoodsEntity::class,
        StoreEntity::class,
        PriceRecordEntity::class,
        ReceiptEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class PriceKeeperDatabase : RoomDatabase() {

    abstract fun goodsDao(): GoodsDao
    abstract fun storeDao(): StoreDao
    abstract fun priceRecordDao(): PriceRecordDao
    abstract fun receiptDao(): ReceiptDao

    companion object {
        const val DATABASE_NAME = "price_keeper.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE store ADD COLUMN map_url TEXT")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}
