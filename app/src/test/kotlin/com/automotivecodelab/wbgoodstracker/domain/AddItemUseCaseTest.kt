package com.automotivecodelab.wbgoodstracker.domain

import kotlinx.coroutines.runBlocking
import org.junit.Test

class AddItemUseCaseTest {

    @Test
    fun testWrongUrl() {
        val addItemUseCase = AddItemUseCase(
            ItemsRepositoryImplFake(AddItemUseCase.itemsCountLimit - 1),
            UserRepostoryImplFake()
        )
        runBlocking {
            val result = addItemUseCase("wildberries.ru/product/12341") {}
            assert(result.exceptionOrNull() is InvalidUrlException)
        }
    }

    @Test
    fun testWrongVendorCode() {
        val addItemUseCase = AddItemUseCase(
            ItemsRepositoryImplFake(AddItemUseCase.itemsCountLimit - 1),
            UserRepostoryImplFake()
        )
        runBlocking {
            val result = addItemUseCase("12341") {}
            assert(result.exceptionOrNull() is InvalidVendorCodeException)
        }
    }

    @Test
    fun testItemsLimitExceeded() {
        val addItemUseCase = AddItemUseCase(
            ItemsRepositoryImplFake(AddItemUseCase.itemsCountLimit + 1),
            UserRepostoryImplFake()
        )
        runBlocking {
            val result = addItemUseCase("https://wildberries.ru/catalog/123456/detail.aspx") {}
            assert(result.exceptionOrNull() is ItemsQuotaExceededException)
        }
    }
}
