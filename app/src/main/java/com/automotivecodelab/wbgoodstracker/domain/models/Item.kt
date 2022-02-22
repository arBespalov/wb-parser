package com.automotivecodelab.wbgoodstracker.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

//room annotations are not supposed to be here. its better to map different models between layers
@Entity
data class Item(
        @PrimaryKey val _id: String,
        val name: String = "",
        val url: String = "",
        val img: String = "",
        val info: List<Info>? = null,
        val observingTimeInMs: Long = 0,
        val ordersCountSinceObservingStarted: Int = 0,
        val estimatedIncome: Int = 0,
        val averageOrdersCountInDay: Int = 0,
        val averagePrice: Int = 0,
        val totalQuantity: Int = 0,


        val local_creationTimeInMs: Long,
        val local_ordersCountDelta: String? = null,
        val local_name: String? = null,
        val local_averagePriceDelta: String? = null,
        val local_groupName: String? = null,
        val local_totalQuantityDelta: String? = null
)

data class Info(
        val timeOfCreationInMs: Long,
        val ordersCount: Int,
        val sizes: List<Sizes>
)

data class Sizes (
        val sizeName : String,
        val quantity : Int,
        val price : Int,
        val priceWithSale : Int,
        val storeIds: List<String>?
)