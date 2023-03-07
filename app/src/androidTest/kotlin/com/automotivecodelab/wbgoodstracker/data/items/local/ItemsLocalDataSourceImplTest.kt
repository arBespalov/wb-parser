package com.automotivecodelab.wbgoodstracker.data.items.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.automotivecodelab.wbgoodstracker.data.items.itemWithSizesDbModel
import com.automotivecodelab.wbgoodstracker.data.items.sizeDBModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class ItemsLocalDataSourceImplTest {
    lateinit var db: AppDatabase
    lateinit var itemsLocalDataSourceImpl: ItemsLocalDataSourceImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        itemsLocalDataSourceImpl = ItemsLocalDataSourceImpl(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun updateItem() {
        runBlocking {
            val itemId = "123"
            itemsLocalDataSourceImpl.addItem(itemWithSizesDbModel(itemId))
            val newSizes = listOf(
                sizeDBModel(itemId).copy(sizeName = "m"),
                sizeDBModel(itemId).copy(sizeName = "l")
            )
            itemsLocalDataSourceImpl.updateItem(
                itemWithSizesDbModel(itemId).copy(
                    sizes = newSizes
                )
            )
            assert(
                itemsLocalDataSourceImpl.getItem(itemId).sizes
                    .sortedBy { it.sizeName }
                    .toTypedArray()
                    .contentDeepEquals(
                        newSizes
                            .sortedBy { it.sizeName }
                            .toTypedArray()
                    )
            )
        }
    }
}
