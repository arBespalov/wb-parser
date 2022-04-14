package com.automotivecodelab.wbgoodstracker.domain.models

data class ItemGroups(
    val totalItemsQuantity: Int,
    val groups: List<Pair<String, Int>>
)
