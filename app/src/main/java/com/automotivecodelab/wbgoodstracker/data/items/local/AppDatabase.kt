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
        database.execSQL("ALTER TABLE item ADD COLUMN local_creationTimeInMs INTEGER")
    }
 }
