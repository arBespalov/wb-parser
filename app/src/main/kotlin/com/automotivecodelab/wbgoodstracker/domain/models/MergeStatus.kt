package com.automotivecodelab.wbgoodstracker.domain.models

sealed interface MergeStatus {
    object Idle : MergeStatus
    object InProgress: MergeStatus
    object Success: MergeStatus
    class Error(val t: Throwable): MergeStatus
}