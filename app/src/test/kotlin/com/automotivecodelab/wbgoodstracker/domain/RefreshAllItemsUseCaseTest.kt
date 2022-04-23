package com.automotivecodelab.wbgoodstracker.domain

import kotlinx.coroutines.runBlocking
import org.junit.Test

class RefreshAllItemsUseCaseTest {

    @Test
    operator fun invoke() {
        val refreshAllItemsUseCaseTest = RefreshAllItemsUseCase(
            UserRepostoryImplFake(),
            ItemsRepositoryImplFake(0),
            UsageStatisticsRepositoryImplFake(
                RefreshAllItemsUseCase.REFRESHES_COUNT_WHEN_ASK_FOR_REVIEW - 1
            ),

        )
        runBlocking {
            var callbackTriggered = false
            refreshAllItemsUseCaseTest(
                onAuthenticationFailureCallback = {},
                askUserForReviewCallback = { callbackTriggered = true }
            )
            assert(callbackTriggered)
        }
    }
}
