package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO Item VALUES (" +
                    "'123456', " +
                    "'name', " +
                    "'example.com', " +
                    "'example.com/img', " +
                    "'itemInfo', " +
                    "500000, " +
                    "10, " +
                    "10000, " +
                    "1, " +
                    "1000, " +
                    "10, " +
                    "1000000, " +
                    "'+1', " +
                    "'local_name', " +
                    "'-1000', " +
                    "'group_name', " +
                    "'-5')"
            )
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    }
}
