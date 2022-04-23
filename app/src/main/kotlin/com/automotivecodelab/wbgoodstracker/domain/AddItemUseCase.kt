package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import java.lang.Exception
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AddItemUseCase @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
) {

    companion object {
        const val itemsCountLimit = 1000
    }

    suspend operator fun invoke(
        input: String,
        onAuthenticationFailureCallback: () -> Unit
    ): Result<Unit> {
        val url = when {
            // isDigitsOnly() is in androidx library, so unit test fails and clean arch rule
            // violates
            input.all { it.isDigit() } -> {
                if (input.length < 6 || input.length > 9)
                    return Result.failure(InvalidVendorCodeException())
                "https://wildberries.ru/catalog/$input/detail.aspx"
            }
            else -> {
                val isUrlValid = (
                    input.contains("https://wildberries.") ||
                        input.contains("https://www.wildberries.") ||
                        input.contains("http://wildberries.") ||
                        input.contains("http://www.wildberries.")
                    ) &&
                    input.contains("/catalog/")
                if (!isUrlValid) return Result.failure(InvalidUrlException())
                // removing sku name before url when copying from official wb app
                input.replaceBefore("http", "")
            }
        }

        val totalItemsCount = itemsRepository.observeGroups().first().totalItemsQuantity
        if (totalItemsCount > itemsCountLimit) return Result.failure(ItemsQuotaExceededException())

        return if (userRepository.isUserAuthenticated()) {
            val user = userRepository.getUser()
            if (user.isFailure) {
                onAuthenticationFailureCallback()
                itemsRepository.addItem(url)
            } else {
                itemsRepository.addItem(url, user.getOrThrow().idToken)
            }
        } else {
            itemsRepository.addItem(url)
        }
    }
}

class InvalidUrlException : Exception("InvalidUrlException")
class ItemsQuotaExceededException : Exception("ItemsQuotaExceededException")
class InvalidVendorCodeException : Exception("InvalidVendorCodeException")
