package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import java.lang.Exception

class AddItemUseCase(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        url: String,
        onAuthenticationFailureCallback: () -> Unit = {}
    ): Result<Unit> {
        if (!url.contains("https://wildberries.") &&
            !url.contains("https://www.wildberries.") &&
            !url.contains("http://wildberries.") &&
            !url.contains("http://www.wildberries.")
        ) {
            return Result.failure(InvalidUrlException())
        }
        val pureUrl = url.replaceBefore("http", "")
        val result = userRepository.getUser()
        return if (result.isFailure) {
            onAuthenticationFailureCallback.invoke()
            itemsRepository.addItem(pureUrl)
        } else {
            val user = result.getOrNull()
            if (user != null) {
                itemsRepository.addItem(
                    pureUrl,
                    user.idToken
                )
            } else {
                itemsRepository.addItem(pureUrl)
            }
        }
    }
}

class InvalidUrlException : Exception()
