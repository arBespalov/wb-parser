package com.automotivecodelab.wbgoodstracker.data.items.local

import com.automotivecodelab.wbgoodstracker.domain.models.Ad
import kotlinx.coroutines.flow.Flow

interface AdLocalDataSource {
    fun observeAd(): Flow<Ad?>
    suspend fun setAd(ad: Ad?)
}