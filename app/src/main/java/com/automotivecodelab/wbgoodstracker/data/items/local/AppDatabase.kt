package com.automotivecodelab.wbgoodstracker.data.items.local

import android.content.Context
import androidx.room.*
import com.automotivecodelab.wbgoodstracker.domain.models.Item

@Database(
    entities = [Item::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

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
//                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .build()
    }
}

// val MIGRATION_1_2 = object : Migration(1, 2) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE item ADD COLUMN local_creationTimeInMs INTEGER")
//    }
// }
//
// val MIGRATION_2_3 = object : Migration(2, 3) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE item ADD COLUMN averagePrice INTEGER")
//    }
// }
//
// val MIGRATION_3_4 = object : Migration(3, 4) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE item ADD COLUMN local_ordersCountDelta INTEGER")
//        database.execSQL("ALTER TABLE item ADD COLUMN local_name TEXT")
//    }
// }
//
// val MIGRATION_4_5 = object : Migration(4, 5) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE item ADD COLUMN local_averagePriceDelta TEXT")
//    }
// }
//
// val MIGRATION_5_6 = object : Migration(5, 6) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE item ADD COLUMN totalQuantity INTEGER")
//    }
// }
//
// val MIGRATION_6_7 = object : Migration(6, 7) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL("ALTER TABLE item ADD COLUMN local_GroupName TEXT")
//    }
// }
