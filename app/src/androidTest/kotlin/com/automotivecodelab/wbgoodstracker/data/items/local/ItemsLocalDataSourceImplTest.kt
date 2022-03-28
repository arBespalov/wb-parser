package com.automotivecodelab.wbgoodstracker.data.items.local

import android.content.Context
import android.content.SharedPreferences
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class ItemsLocalDataSourceImplTest {
    lateinit var db: AppDatabase
    lateinit var itemsLocalDataSourceImpl: ItemsLocalDataSourceImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        itemsLocalDataSourceImpl = ItemsLocalDataSourceImpl(
            db.itemDao(),
            db.sizeDao(),
            context.getSharedPreferences("sp", Context.MODE_PRIVATE)
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun updateItem() {
        val itemId = "id"
        val sizeDBModel = SizeDBModel(
            itemId = itemId,
            price = 1000,
            priceWithSale = 800,
            quantity = 50,
            sizeName = "",
            storesWithQuantity = ""
        )
        val item = ItemWithSizesDBModel(
            item = ItemDBModel(
                        id = itemId,
                        name = "name",
                        url = "example.com",
                        img = "example.com/img.png",
                        observingTimeInMs = 1000,
                        ordersCountSinceObservingStarted = 0,
                        estimatedIncome = 0,
                        averageOrdersCountPerDay = 0,
                        averagePrice = 500,
                        totalQuantity = 100,
                        creationTimestamp = Date().time,
                        ordersCountDelta = null,
                        localName = null,
                        averagePriceDelta = null,
                        groupName = null,
                        totalQuantityDelta = null,
                        lastUpdateTimestamp = 1000,
                        ordersCount = 0,
            ),
            sizes = listOf(
                sizeDBModel.copy(sizeName = "s"),
                sizeDBModel.copy(sizeName = "m")
            )
        )
        runBlocking {
            itemsLocalDataSourceImpl.addItem(item)
            val newSizes = listOf(
                sizeDBModel.copy(sizeName = "m"),
                sizeDBModel.copy(sizeName = "l")
            )
            itemsLocalDataSourceImpl.updateItem(item.copy(
                sizes = newSizes
            ))
            assert(itemsLocalDataSourceImpl.getItem(itemId).sizes
                .sortedBy { it.sizeName }
                .toTypedArray()
                .contentDeepEquals (newSizes
                    .sortedBy { it.sizeName }
                    .toTypedArray()))
        }
    }
}