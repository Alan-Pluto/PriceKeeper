package com.pricekeeper.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pricekeeper.app.data.local.dao.GoodsDao
import com.pricekeeper.app.data.local.dao.PriceRecordDao
import com.pricekeeper.app.data.local.dao.StoreDao
import com.pricekeeper.app.data.local.entity.GoodsEntity
import com.pricekeeper.app.data.local.entity.PriceRecordEntity
import com.pricekeeper.app.data.local.entity.StoreEntity

@Database(
    entities = [
        GoodsEntity::class,
        StoreEntity::class,
        PriceRecordEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class PriceKeeperDatabase : RoomDatabase() {

    abstract fun goodsDao(): GoodsDao
    abstract fun storeDao(): StoreDao
    abstract fun priceRecordDao(): PriceRecordDao

    companion object {
        const val DATABASE_NAME = "price_keeper.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE store ADD COLUMN map_url TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS price_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        goods_id INTEGER NOT NULL,
                        store_id INTEGER NOT NULL,
                        price REAL NOT NULL,
                        record_date INTEGER NOT NULL,
                        is_promotion INTEGER NOT NULL,
                        note TEXT,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(goods_id) REFERENCES goods(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(store_id) REFERENCES store(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO price_records_new (
                        id, goods_id, store_id, price, record_date, is_promotion, note, created_at
                    )
                    SELECT id, goods_id, store_id, price, record_date, is_promotion, note, created_at
                    FROM price_records
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE price_records")
                db.execSQL("ALTER TABLE price_records_new RENAME TO price_records")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_price_records_goods_id ON price_records(goods_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_price_records_store_id ON price_records(store_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_price_records_record_date ON price_records(record_date)")
                db.execSQL("DROP TABLE IF EXISTS receipts")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}
