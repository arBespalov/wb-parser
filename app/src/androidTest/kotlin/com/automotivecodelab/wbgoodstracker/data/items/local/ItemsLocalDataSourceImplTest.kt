package com.automotivecodelab.wbgoodstracker.data.items.local

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.automotivecodelab.wbgoodstracker.data.items.itemWithSizesDbModel
import com.automotivecodelab.wbgoodstracker.data.items.sizeDBModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.coroutines.coroutineContext

class ItemsLocalDataSourceImplTest {
    lateinit var db: AppDatabase
    lateinit var itemsLocalDataSourceImpl: ItemsLocalDataSourceImpl
    lateinit var testDataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile (UUID.randomUUID().toString()) }
        )
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        itemsLocalDataSourceImpl = ItemsLocalDataSourceImpl(testDataStore, db)
    }

    @After
    fun tearDown() {
        db.close()
        runBlocking {
            testDataStore.edit {
                it.clear()
            }
        }
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
            assert(itemsLocalDataSourceImpl.getItem(itemId).sizes
                .sortedBy { it.sizeName }
                .toTypedArray()
                .contentDeepEquals (newSizes
                    .sortedBy { it.sizeName }
                    .toTypedArray()))
        }
    }
}