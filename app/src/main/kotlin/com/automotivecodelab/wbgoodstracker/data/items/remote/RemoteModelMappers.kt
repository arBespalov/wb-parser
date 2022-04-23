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
    previousLastChangesTimestamp: Long,
    previousSizeQuantity: Map<String, Int>?, // sizeName, quantity
    previousFeedbacks: Int
): ItemWithSizesDBModel {
    val totalQuantityDelta = totalQuantity - previousTotalQuantity
    val ordersCountDelta = info[0].ordersCount - previousOrdersCount
    val averagePriceDelta = averagePrice - previousAveragePrice
    val quantityDelta = info[0].sizes.associate {
        it.sizeName to it.quantity - (previousSizeQuantity?.get(it.sizeName) ?: it.quantity)
    }
    val feedbacksDelta = feedbacks - previousFeedbacks

    val lastChangesTimestamp = if (totalQuantityDelta == 0 &&
        ordersCountDelta == 0 &&
        averagePriceDelta == 0 &&
        quantityDelta.all { (_, delta) -> delta == 0 } &&
        feedbacksDelta == 0
    )
        previousLastChangesTimestamp
    else
        Date().time
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
            lastChangesTimestamp = lastChangesTimestamp,
            lastUpdateTimestamp = info[0].timeOfCreationInMs,
            ordersCount = info[0].ordersCount,
            feedbacks = feedbacks,
            feedbacksDelta = feedbacksDelta,
            updateError = updateError,
        ),
        sizes = info[0].sizes.map {
            SizeDBModel(
                itemId = _id,
                sizeName = it.sizeName,
                quantity = it.quantity,
                quantityDelta = quantityDelta[it.sizeName] ?: 0,
                price = it.price,
                priceWithSale = it.priceWithSale,
                storesWithQuantity = it.storeIds?.joinToString(" \u2022 ")
            )
        }
    )
}
