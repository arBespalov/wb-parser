package com.automotivecodelab.wbgoodstracker.data.items.local

import kotlinx.coroutines.flow.Flow

interface CurrentGroupLocalDataSource {
    fun observeCurrentGroup(): Flow<String?>
    suspend fun setCurrentGroup(groupName: String?)
}