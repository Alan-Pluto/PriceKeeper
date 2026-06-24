package com.pricekeeper.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_records",
    foreignKeys = [
        ForeignKey(
            entity = GoodsEntity::class,
            parentColumns = ["id"],
            childColumns = ["goods_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["store_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["goods_id"]),
        Index(value = ["store_id"]),
        Index(value = ["record_date"])
    ]
)
data class PriceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "goods_id")
    val goodsId: Long,

    @ColumnInfo(name = "store_id")
    val storeId: Long,

    val price: Double,

    @ColumnInfo(name = "record_date")
    val recordDate: Long,

    @ColumnInfo(name = "is_promotion")
    val isPromotion: Boolean = false,

    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
