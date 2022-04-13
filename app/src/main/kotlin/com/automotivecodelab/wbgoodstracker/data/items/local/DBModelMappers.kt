package com.automotivecodelab.wbgoodstracker.data.items.local

import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.domain.models.Size

fun ItemWithSizesDBModel.toDomainModel() = Item(
    id = item.id,
    name = item.name,
    url = item.url,
    img = item.img,
    observingTimeInMs = item.observingTimeInMs,
    ordersCountSinceObservingStarted = item.ordersCountSinceObservingStarted,
    estimatedIncome = item.estimatedIncome,
    averageOrdersCountPerDay = item.averageOrdersCountPerDay,
    averagePrice = item.averagePrice,
    totalQuantity = item.totalQuantity,
    creationTimestamp = item.creationTimestamp,
    ordersCountDelta = item.ordersCountDelta,
    localName = item.localName,
    averagePriceDelta = item.averagePriceDelta,
    groupName = item.groupName,
    totalQuantityDelta = item.totalQuantityDelta,
    lastTotalQuantityDeltaUpdateTimestamp = item.lastTotalQuantityDeltaUpdateTimestamp,
    lastUpdateTimestamp = item.lastUpdateTimestamp,
    ordersCount = item.ordersCount,
    sizes = sizes.map {
        Size(
            sizeName = it.sizeName,
            quantity = it.quantity,
            quantityDelta = it.quantityDelta,
            price = it.price,
            priceWithSale = it.priceWithSale,
            storesWithQuantity = it.storesWithQuantity
        )
    }
)

fun Item.toDBModel() = ItemWithSizesDBModel(
    item = ItemDBModel(
        id = id,
        name = name,
        url = url,
        img = img,
        observingTimeInMs = observingTimeInMs,
        ordersCountSinceObservingStarted = ordersCountSinceObservingStarted,
        estimatedIncome = estimatedIncome,
        averageOrdersCountPerDay = averageOrdersCountPerDay,
        averagePrice = averagePrice,
        totalQuantity = totalQuantity,
        creationTimestamp = creationTimestamp,
        ordersCountDelta = ordersCountDelta,
        localName = localName,
        averagePriceDelta = averagePriceDelta,
        groupName = groupName,
        totalQuantityDelta = totalQuantityDelta,
        lastTotalQuantityDeltaUpdateTimestamp = lastTotalQuantityDeltaUpdateTimestamp,
        lastUpdateTimestamp = lastUpdateTimestamp,
        ordersCount = ordersCount,
    ),
    sizes = sizes.map {
        SizeDBModel(
            itemId = id,
            sizeName = it.sizeName,
            quantity = it.quantity,
            quantityDelta = it.quantityDelta,
            price = it.price,
            priceWithSale = it.priceWithSale,
            storesWithQuantity = it.storesWithQuantity
        )
    }
)