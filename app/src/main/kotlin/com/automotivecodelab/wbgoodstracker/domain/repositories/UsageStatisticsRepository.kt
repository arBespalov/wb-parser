package com.automotivecodelab.wbgoodstracker.domain.repositories

interface UsageStatisticsRepository {
    suspend fun incrementUpdateAllItemsAction()
    suspend fun getCountOfUpdateAllItemsAction(): Int
}