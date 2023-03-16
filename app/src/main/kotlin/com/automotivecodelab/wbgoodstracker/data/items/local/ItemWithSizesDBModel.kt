package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(tableName = "item")
data class ItemDBModel(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    val img: String,
    val observingTimeInMs: Long,
    val ordersCountSinceObservingStarted: Int,
    val estimatedIncome: Int,
    val averageOrdersCountPerDay: Int,
    val averagePrice: Int,
    val totalQuantity: Int,
    val creationTimestamp: Long,
    val ordersCountDelta: Int,
    val localName: String?,
    val averagePriceDelta: Int,
    val groupName: String?,
    val totalQuantityDelta: Int,
    val lastChangesTimestamp: Long,
    val lastUpdateTimestamp: Long,
    val ordersCount: Int,
    val feedbacks: Int,
    val feedbacksDelta: Int,
    val updateError: Boolean?
)

@Entity(
    tableName = "size",
    primaryKeys = ["itemId", "sizeName"],
    foreignKeys = [
        ForeignKey(
            entity = ItemDBModel::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = CASCADE
        )
    ]
)
data class SizeDBModel(
    val itemId: String,
    val sizeName: String,
    val quantity: Int,
    val quantityDelta: Int,
    val price: Int,
    val priceWithSale: Int,
    val storesWithQuantity: String?
)

data class ItemWithSizesDBModel(
    @Embedded val item: ItemDBModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val sizes: List<SizeDBModel>
)

data class GroupNameWithCount(
    val groupName: String?,
    val count: Int
)
