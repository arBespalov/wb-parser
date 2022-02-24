package com.automotivecodelab.wbgoodstracker.ui.chartfragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.automotivecodelab.wbgoodstracker.domain.GetOrdersChartDataUseCase
import com.automotivecodelab.wbgoodstracker.domain.ObserveSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.RefreshSingleItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.ui.detailfrag.DetailViewModel

class ChartViewModelFactory(
    private val itemsRepository: ItemsRepository,
    private val itemId: String
): ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChartViewModel(
            GetOrdersChartDataUseCase(itemsRepository),
            itemId
        ) as T
    }
}