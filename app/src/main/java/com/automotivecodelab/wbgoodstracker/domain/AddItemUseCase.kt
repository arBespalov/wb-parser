package com.automotivecodelab.wbgoodstracker.domain

import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import com.automotivecodelab.wbgoodstracker.domain.util.Result
import java.lang.Exception

class AddItemUseCase(
    private val itemsRepository: ItemsRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        url: String,
        groupName: String,
        onAuthenticationFailureCallback: () -> Unit = {}
    ): Result<Unit> {
        if (url.contains("https://wildberries.") ||
            url.contains("https://www.wildberries.") ||
            url.contains("http://wildberries.") ||
            url.contains("http://www.wildberries.")
        ) {
            val pureUrl = url.replaceBefore("http", "")
            return when (val authenticationResult = userRepository.getUser()) {
                is Result.Error -> {
                    onAuthenticationFailureCallback.invoke()
                    itemsRepository.addItem(pureUrl, groupName)
                }
                is Result.Success<User?> -> {
                    if (authenticationResult.data != null) {
                        itemsRepository.addItem(
                            pureUrl,
                            groupName,
                            authenticationResult.data.idToken
                        )
                    } else {
                        itemsRepository.addItem(pureUrl, groupName)
                    }
                }
            }
        } else {
            return Result.Error(InvalidUrlException())
        }
    }
}

class InvalidUrlException : Exception()
