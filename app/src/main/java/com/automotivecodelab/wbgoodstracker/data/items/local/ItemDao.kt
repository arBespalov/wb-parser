package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.automotivecodelab.wbgoodstracker.domain.models.Item

@Dao
interface ItemDao {
    @Query("SELECT * FROM item")
    fun observeAll(): LiveData<List<Item>>

    @Query("SELECT * FROM item")
    fun getAll(): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Item)

    @Delete
    fun delete(vararg items: Item)

    @Update
    fun update(item: Item): Int

    @Update
    fun batchUpdate(items: List<Item>): Int

    @Query("SELECT * FROM item WHERE _id IN (:id)")
    fun getById(id: String): Item

    @Query("SELECT * FROM item WHERE _id IN (:id)")
    fun observeById(id: String): LiveData<Item>

    @Query("SELECT * FROM item WHERE local_groupName IN (:group)")
    fun observeByGroup(group: String): LiveData<List<Item>>

    @Query("SELECT * FROM item WHERE local_groupName IN (:group)")
    fun getByGroup(group: String): List<Item>
}