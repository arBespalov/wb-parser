package com.automotivecodelab.wbgoodstracker.di

import com.automotivecodelab.wbgoodstracker.data.items.ItemsRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSource
import com.automotivecodelab.wbgoodstracker.data.items.local.ItemsLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSource
import com.automotivecodelab.wbgoodstracker.data.items.remote.ItemsRemoteDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.sort.SortLocalDataSource
import com.automotivecodelab.wbgoodstracker.data.sort.SortLocalDataSourceImpl
import com.automotivecodelab.wbgoodstracker.data.sort.SortRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.usagestatistics.UsageStatisticsRepositoryImpl
import com.automotivecodelab.wbgoodstracker.data.user.AuthenticationService
import com.automotivecodelab.wbgoodstracker.data.user.AuthenticationServiceImpl
import com.automotivecodelab.wbgoodstracker.data.user.UserRepositoryImpl
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UsageStatisticsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import dagger.Binds
import dagger.Module

@Module
interface AppModule {
    @Binds
    fun bindItemsLocalDataSource(impl: ItemsLocalDataSourceImpl): ItemsLocalDataSource

    @Binds
    fun bindItemsRemoteDataSource(impl: ItemsRemoteDataSourceImpl): ItemsRemoteDataSource

    @Binds
    fun bindSortLocalDataSource(impl: SortLocalDataSourceImpl): SortLocalDataSource

    @Binds
    fun bindAuthenticationService(impl: AuthenticationServiceImpl): AuthenticationService

    // repositories
    @Binds
    fun bindItemsRepository(impl: ItemsRepositoryImpl): ItemsRepository

    @Binds
    fun bindSortRepository(impl: SortRepositoryImpl): SortRepository

    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    fun bindUsageStatisticsRepository(impl: UsageStatisticsRepositoryImpl)
            : UsageStatisticsRepository
}