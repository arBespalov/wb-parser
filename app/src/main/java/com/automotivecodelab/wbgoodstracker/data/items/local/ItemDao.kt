package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ItemDao {
    //todo make suspend
    @Transaction
    @Query("SELECT * FROM item")
    fun observeAll(): LiveData<List<ItemWithSizesDBModel>>

    @Transaction
    @Query("SELECT * FROM item")
    fun getAll(): List<ItemWithSizesDBModel>

    @Transaction
    @Query("SELECT * FROM item WHERE id IN (:id)")
    fun getById(id: String): ItemWithSizesDBModel

    @Transaction
    @Query("SELECT * FROM item WHERE id IN (:id)")
    fun observeById(id: String): LiveData<ItemWithSizesDBModel>

    @Transaction
    @Query("SELECT * FROM item WHERE groupName IN (:group)")
    fun observeByGroup(group: String): LiveData<List<ItemWithSizesDBModel>>

    @Transaction
    @Query("SELECT * FROM item WHERE groupName IN (:group)")
    fun getByGroup(group: String): List<ItemWithSizesDBModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg item: ItemDBModel)

    @Delete
    fun delete(vararg item: ItemDBModel)

    @Update
    fun update(vararg item: ItemDBModel): Int
}
