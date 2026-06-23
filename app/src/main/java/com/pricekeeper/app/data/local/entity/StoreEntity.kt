package com.pricekeeper.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "store",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["region"])
    ]
)
data class StoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val region: String,

    val address: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    @ColumnInfo(name = "map_url")
    val mapUrl: String? = null,

    @ColumnInfo(name = "my_note")
    val myNote: String? = null,

    val rating: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
