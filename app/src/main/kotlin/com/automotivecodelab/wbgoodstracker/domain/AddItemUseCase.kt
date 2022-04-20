package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import kotlinx.coroutines.flow.first
import java.lang.Exception
import javax.inject.Inject

class AddItemUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
) {

    companion object {
        const val itemsCountLimit = 1000
    }

    suspend operator fun invoke(
        url: String,
        onAuthenticationFailureCallback: () -> Unit
    ): Result<Unit> {
        val isUrlValid = (url.contains("https://wildberries.") ||
                url.contains("https://www.wildberries.") ||
                url.contains("http://wildberries.") ||
                url.contains("http://www.wildberries.")) &&
                url.contains("/catalog/")
        if (!isUrlValid) return Result.failure(InvalidUrlException())
        val totalItemsCount = itemsRepository.observeGroups().first().totalItemsQuantity
        if (totalItemsCount > itemsCountLimit) return Result.failure(ItemsQuotaExceededException())
        // removing sku name before url when copying from official wb app
        val pureUrl = url.replaceBefore("http", "")
        return if (userRepository.isUserAuthenticated()) {
            val user = userRepository.getUser()
            if (user.isFailure) {
                onAuthenticationFailureCallback()
                itemsRepository.addItem(pureUrl)
            } else {
                itemsRepository.addItem(pureUrl, user.getOrThrow().idToken)
            }
        } else {
            itemsRepository.addItem(pureUrl)
        }
    }
}

class InvalidUrlException : Exception("InvalidUrlException")
class ItemsQuotaExceededException: Exception("ItemsQuotaExceededException")