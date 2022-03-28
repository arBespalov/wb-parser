package com.automotivecodelab.wbgoodstracker.data.items.remote

import com.automotivecodelab.wbgoodstracker.data.items.local.ItemDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemWithSizesDBModel
import com.automotivecodelab.wbgoodstracker.data.items.local.SizeDBModel

fun ItemRemoteModel.toDBModel(
    creationTimestamp: Long,
    previousOrdersCount: Int,
    previousAveragePrice: Int,
    previousTotalQuantity: Int,
    localName: String?,
    groupName: String?
): ItemWithSizesDBModel {

    val ordersCountDelta = info[0].ordersCount - previousOrdersCount
    val sOrdersCountDelta: String? = when {
        ordersCountDelta < 0 -> "$ordersCountDelta"
        ordersCountDelta > 0 -> "+$ordersCountDelta"
        else -> null
    }

    val averagePriceDelta = averagePrice - previousAveragePrice
    val sAveragePriceDelta: String? = when {
        averagePriceDelta < 0 -> "$averagePriceDelta"
        averagePriceDelta > 0 -> "+$averagePriceDelta"
        else -> null
    }

    val totalQuantityDelta = totalQuantity - previousTotalQuantity
    val sTotalQuantityDelta: String? = when {
        totalQuantityDelta < 0 -> "$totalQuantityDelta"
        totalQuantityDelta > 0 -> "+$totalQuantityDelta"
        else -> null
    }

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
            ordersCountDelta = sOrdersCountDelta,
            localName = localName,
            averagePriceDelta = sAveragePriceDelta,
            groupName = groupName,
            totalQuantityDelta = sTotalQuantityDelta,
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