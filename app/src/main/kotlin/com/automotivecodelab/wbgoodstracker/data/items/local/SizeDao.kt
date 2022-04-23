package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.room.*

@Dao
interface SizeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg size: SizeDBModel)

    @Delete
    suspend fun delete(vararg size: SizeDBModel)

    @Update
    suspend fun update(vararg size: SizeDBModel): Int
}
