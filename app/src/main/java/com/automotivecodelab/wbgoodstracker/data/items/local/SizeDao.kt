package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.room.*

@Dao
interface SizeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg size: SizeDBModel)

    @Delete
    fun delete(vararg size: SizeDBModel)

    @Update
    fun update(vararg size: SizeDBModel): Int
}