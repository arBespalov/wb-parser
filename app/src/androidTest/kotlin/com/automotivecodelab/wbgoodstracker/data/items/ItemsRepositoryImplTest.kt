package com.automotivecodelab.wbgoodstracker.data.items

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.automotivecodelab.wbgoodstracker.data.items.local.AppDatabase
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemInfoRemoteModel
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemRemoteModel
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSource
import com.automotivecodelab.wbgoodstracker.data.items.remote.SizeRemoteModel
import java.util.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class ItemsRepositoryImplTest {

    lateinit var db: AppDatabase
    lateinit var testDataStore: DataStore<Preferences>
    lateinit var itemsLocalDataSourceImpl: ItemsLocalDataSourceImpl
    lateinit var itemsRepositoryImpl: ItemsRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDataStore = PreferenceDataStoreFactory.create(
            // without random filename test crashes when running whole class
            produceFile = { context.preferencesDataStoreFile(UUID.randomUUID().toString()) }
        )
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        itemsLocalDataSourceImpl = ItemsLocalDataSourceImpl(testDataStore, db)
        val itemsRemoteDataSource = ItemsRemoteDataSourceFake()
        itemsRepositoryImpl = ItemsRepositoryImpl(itemsLocalDataSourceImpl, itemsRemoteDataSource)
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
    fun addNewItem() {
        val itemUrl = "example.com/item"
        runBlocking {
            itemsRepositoryImpl.addItem(itemUrl)
            val (items, _) = itemsRepositoryImpl.observeItems().first()
            assert(items.size == 1 && items[0].url == itemUrl)
        }
    }

    @Test
    fun addExistingItem() {
        val itemUrl = "example.com/item"
        runBlocking {
            itemsRepositoryImpl.addItem(itemUrl)
            itemsRepositoryImpl.addItem(itemUrl)
            val (items, _) = itemsRepositoryImpl.observeItems().first()
            assert(items.size == 1 && items[0].url == itemUrl)
        }
    }

    @Test
    fun syncItems() {
        val itemIdToDelete = "789"
        runBlocking {
            itemsLocalDataSourceImpl.addItem(
                itemWithSizesDbModel(itemIdToDelete)
            )
            itemsLocalDataSourceImpl.addItem(
                itemWithSizesDbModel(ItemsRemoteDataSourceFake.ID1)
            )
            itemsRepositoryImpl.syncItems("token")
            val (items, _) = itemsRepositoryImpl.observeItems().first()
            assert(
                items.size == 2 &&
                    items.any { it.id == ItemsRemoteDataSourceFake.ID1 } &&
                    items.any { it.id == ItemsRemoteDataSourceFake.ID2 }
            )
        }
    }

    @Test
    fun mergeItems() {
        val existingItemId = "789"
        runBlocking {
            itemsLocalDataSourceImpl.addItem(
                itemWithSizesDbModel(existingItemId)
            )
            itemsLocalDataSourceImpl.addItem(
                itemWithSizesDbModel(ItemsRemoteDataSourceFake.ID1)
            )
            val result = itemsRepositoryImpl.mergeItems("token")
            val (items, _) = itemsRepositoryImpl.observeItems().first()
            assert(items.size == 3)
            assert(
                items.size == 3 &&
                    items.any { it.id == ItemsRemoteDataSourceFake.ID1 } &&
                    items.any { it.id == ItemsRemoteDataSourceFake.ID2 } &&
                    items.any { it.id == existingItemId }
            )
        }
    }
}

class ItemsRemoteDataSourceFake : ItemsRemoteDataSource {

    companion object {
        const val ID1 = "123"
        const val ID2 = "456"
    }

    private val itemRemoteModelFake = ItemRemoteModel(
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

    override suspend fun addItem(url: String, idToken: String?): ItemRemoteModel {
        return itemRemoteModelFake.copy(url = url)
    }

    override suspend fun deleteItems(itemsId: List<Int>, idToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getItemsForUserId(idToken: String): List<ItemRemoteModel> {
        return listOf(
            itemRemoteModelFake.copy(_id = ID1),
            itemRemoteModelFake.copy(_id = ID2)
        )
    }

    override suspend fun updateItems(itemsId: List<Int>): List<ItemRemoteModel> {
        return itemsId.map { id ->
            itemRemoteModelFake.copy(_id = id.toString())
        }
    }

    override suspend fun mergeItems(itemsId: List<Int>, idToken: String): List<ItemRemoteModel> {
        return itemsId
            .map { id ->
                itemRemoteModelFake.copy(_id = id.toString())
            }
            .plus(itemRemoteModelFake.copy(_id = ID2))
    }

    override suspend fun getItemWithFullData(itemId: String): ItemRemoteModel {
        TODO("Not yet implemented")
    }

    override suspend fun mergeItemsDebug(
        itemsId: List<Int>,
        userId: String
    ): List<ItemRemoteModel> {
        TODO("Not yet implemented")
    }
}
