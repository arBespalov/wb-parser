package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Transaction
    @Query("SELECT * FROM item")
    fun observeAll(): Flow<List<ItemWithSizesDBModel>>

    @Transaction
    @Query("SELECT * FROM item")
    suspend fun getAll(): List<ItemWithSizesDBModel>

    @Transaction
    @Query("SELECT * FROM item WHERE id IN (:id)")
    suspend fun getById(id: String): ItemWithSizesDBModel?

    @Transaction
    @Query("SELECT * FROM item WHERE id IN (:id)")
    fun observeById(id: String): Flow<ItemWithSizesDBModel?>

    @Transaction
    @Query("SELECT * FROM item WHERE groupName IN (:group)")
    fun observeByGroup(group: String): Flow<List<ItemWithSizesDBModel>>

    @Transaction
    @Query("SELECT * FROM item WHERE groupName IN (:group)")
    suspend fun getByGroup(group: String): List<ItemWithSizesDBModel>

    @Query("SELECT groupName, COUNT(*) AS count FROM item GROUP BY groupName")
    fun getGroups(): Flow<List<GroupNameWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg item: ItemDBModel)

    @Delete
    suspend fun delete(vararg item: ItemDBModel)

    @Update
    suspend fun update(vararg item: ItemDBModel): Int
}
