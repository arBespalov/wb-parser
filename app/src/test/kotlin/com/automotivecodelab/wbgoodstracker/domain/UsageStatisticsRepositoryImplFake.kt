package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.UsageStatisticsRepository

class UsageStatisticsRepositoryImplFake(var counter: Int) : UsageStatisticsRepository {
    override suspend fun incrementUpdateAllItemsAction() {
        counter++
    }

    override suspend fun getCountOfUpdateAllItemsAction(): Int {
        return counter
    }
}
