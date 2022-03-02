package com.automotivecodelab.wbgoodstracker.data.items.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.automotivecodelab.wbgoodstracker.domain.models.Item

@Database(
    entities = [ItemDBModel::class, SizeDBModel::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun sizeDao(): SizeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE item_new ( " +
                "id TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "url TEXT NOT NULL, " +
                "img TEXT NOT NULL, " +
                "observingTimeInMs INTEGER NOT NULL, " +
                "ordersCountSinceObservingStarted INTEGER NOT NULL, " +
                "estimatedIncome INTEGER NOT NULL, " +
                "averageOrdersCountPerDay INTEGER NOT NULL, " +
                "averagePrice INTEGER NOT NULL, " +
                "totalQuantity INTEGER NOT NULL, " +
                "creationTimestamp INTEGER NOT NULL, " +
                "ordersCountDelta TEXT, " +
                "localName TEXT, " +
                "averagePriceDelta TEXT, " +
                "groupName TEXT, " +
                "totalQuantityDelta TEXT, " +
                "lastUpdateTimestamp INTEGER NOT NULL DEFAULT 0, " +
                "ordersCount INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (id))"
        )
        database.execSQL("INSERT INTO item_new (" +
                "id, " +
                "name, " +
                "url, " +
                "img, " +
                "observingTimeInMs, " +
                "ordersCountSinceObservingStarted, " +
                "estimatedIncome, " +
                "averageOrdersCountPerDay, " +
                "averagePrice, " +
                "totalQuantity, " +
                "creationTimestamp, " +
                //"ordersCountDelta, " +
                "localName, " +
                //"averagePriceDelta, " +
                "groupName) " +
                //"totalQuantityDelta, " +
                //"lastUpdateTimestamp, " +
                //"ordersCount) " +
                "SELECT " +
                "_id, " +
                "name, " +
                "url, " +
                "img, " +
                "observingTimeInMs, " +
                "ordersCountSinceObservingStarted, " +
                "estimatedIncome, " +
                "averageOrdersCountInDay, " +
                "averagePrice, " +
                "totalQuantity, " +
                "local_creationTimeInMs, " +
                "local_name, " +
                "local_groupName " +
                "FROM item"
        )
        database.execSQL("DROP TABLE item")
        database.execSQL("ALTER TABLE item_new RENAME TO item")
        database.execSQL("CREATE TABLE size (" +
                "itemId TEXT NOT NULL, " +
                "sizeName TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "price INTEGER NOT NULL, " +
                "priceWithSale INTEGER NOT NULL, " +
                "storesWithQuantity TEXT, " +
                "PRIMARY KEY (itemId, sizeName), " +
                "FOREIGN KEY (itemId) REFERENCES item (id) ON DELETE CASCADE)"
        )
    }
}
