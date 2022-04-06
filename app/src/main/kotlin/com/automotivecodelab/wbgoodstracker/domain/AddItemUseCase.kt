package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import java.lang.Exception
import javax.inject.Inject

class AddItemUseCase @Inject constructor(
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
        return if (userRepository.isUserAuthenticated()) {
            val user = userRepository.getUser()
            if (user.isFailure) {
                onAuthenticationFailureCallback.invoke()
                itemsRepository.addItem(pureUrl)
            } else {
                itemsRepository.addItem(pureUrl, user.getOrThrow().idToken)
            }
        } else {
            itemsRepository.addItem(pureUrl)
        }
    }
}

class InvalidUrlException : Exception()
