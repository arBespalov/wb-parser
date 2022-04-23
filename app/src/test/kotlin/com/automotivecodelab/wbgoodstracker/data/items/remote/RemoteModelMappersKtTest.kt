package com.automotivecodelab.wbgoodstracker.data.items.remote

import java.util.*
import org.junit.Assert.*
import org.junit.Test

class RemoteModelMappersKtTest {
    @Test
    fun testDeltas() {
        val itemRemoteModel = ItemRemoteModel(
            _id = "1234567",
            name = "name",
            url = "example.com",
            img = "example.com/example.png",
            info = listOf(
                ItemInfoRemoteModel(
                    timeOfCreationInMs = Date().time,
                    ordersCount = 10,
                    sizes = listOf(
                        SizeRemoteModel(
                            sizeName = "s",
                            quantity = 5,
                            price = 1000,
                            priceWithSale = 800,
                            storeIds = listOf("store1: 2", "store2: 3")
                        ),
                        SizeRemoteModel(
                            sizeName = "m",
                            quantity = 6,
                            price = 1200,
                            priceWithSale = 900,
                            storeIds = listOf("store1: 2", "store3: 4")
                        ),
                        SizeRemoteModel(
                            sizeName = "l",
                            quantity = 10,
                            price = 1200,
                            priceWithSale = 1000,
                            storeIds = listOf("store2: 4", "store3: 6")
                        )
                    )
                )
            ),
            observingTimeInMs = 5000000000,
            ordersCountSinceObservingStarted = 3,
            estimatedIncome = 5000,
            averageOrdersCountInDay = 1,
            averagePrice = 900,
            totalQuantity = 21,
            feedbacks = 2,
            updateError = false
        )
        val previousOrdersCount = 5
        val previousAveragePrice = 800
        val previousLastChangesTimestamp = Date().time
        val previousSizeQuantity = mapOf(
            "s" to 7,
            "m" to 8,
            "l" to 10
        )
        val previousTotalQuantity = previousSizeQuantity.values.sum()
        val previousFeedbacks = 1
        val dbModel = itemRemoteModel.toDBModel(
            creationTimestamp = Date().time,
            previousOrdersCount = previousOrdersCount,
            previousAveragePrice = previousAveragePrice,
            previousTotalQuantity = previousTotalQuantity,
            localName = "item",
            groupName = "groupName",
            previousLastChangesTimestamp = previousLastChangesTimestamp,
            previousSizeQuantity = previousSizeQuantity,
            previousFeedbacks = previousFeedbacks
        )
        assert(
            dbModel.item.ordersCountDelta == itemRemoteModel.info[0].ordersCount -
                previousOrdersCount &&
                dbModel.item.averagePriceDelta == itemRemoteModel.averagePrice -
                previousAveragePrice &&
                dbModel.item.lastUpdateTimestamp != previousLastChangesTimestamp &&
                dbModel.sizes.all { sizeDBModel ->
                    sizeDBModel.quantityDelta ==
                        itemRemoteModel.info[0].sizes
                        .find { it.sizeName == sizeDBModel.sizeName }!!.quantity -
                        previousSizeQuantity[sizeDBModel.sizeName]!!
                } &&
                dbModel.item.totalQuantityDelta == itemRemoteModel.totalQuantity -
                previousTotalQuantity &&
                dbModel.item.feedbacksDelta == itemRemoteModel.feedbacks - previousFeedbacks
        )
    }
}
