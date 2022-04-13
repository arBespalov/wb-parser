package com.automotivecodelab.wbgoodstracker.data.items.remote

import com.automotivecodelab.wbgoodstracker.data.items.local.ItemDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemWithSizesDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.SizeDBModel
import java.util.*

fun ItemRemoteModel.toDBModel(
    creationTimestamp: Long,
    previousOrdersCount: Int,
    previousAveragePrice: Int,
    previousTotalQuantity: Int,
    localName: String?,
    groupName: String?,
    previousLastTotalQuantityDeltaUpdateTimestamp: Long
): ItemWithSizesDBModel {
    val ordersCountDelta = info[0].ordersCount - previousOrdersCount
    val averagePriceDelta = averagePrice - previousAveragePrice
    val totalQuantityDelta = totalQuantity - previousTotalQuantity
    return ItemWithSizesDBModel(
        item = ItemDBModel(
            id = _id,
            name = name,
            url = url,
            img = img,
            observingTimeInMs = observingTimeInMs,
            ordersCountSinceObservingStarted = ordersCountSinceObservingStarted,
            estimatedIncome = estimatedIncome,
            averageOrdersCountPerDay = averageOrdersCountInDay,
            averagePrice = averagePrice,
            totalQuantity = totalQuantity,
            creationTimestamp = creationTimestamp,
            ordersCountDelta = ordersCountDelta,
            localName = localName,
            averagePriceDelta = averagePriceDelta,
            groupName = groupName,
            totalQuantityDelta = totalQuantityDelta,
            lastTotalQuantityDeltaUpdateTimestamp = if (totalQuantityDelta == 0)
                previousLastTotalQuantityDeltaUpdateTimestamp else Date().time,
            lastUpdateTimestamp = info[0].timeOfCreationInMs,
            ordersCount = info[0].ordersCount,
        ),
        sizes = info[0].sizes.map {
            SizeDBModel(
                itemId = _id,
                sizeName = it.sizeName,
                quantity = it.quantity,
                price = it.price,
                priceWithSale = it.priceWithSale,
                storesWithQuantity = it.storeIds?.joinToString(", ")
            )
        }
    )
}