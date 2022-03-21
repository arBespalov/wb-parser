package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import com.automotivecodelab.wbgoodstracker.domain.models.Item

data class EditItemViewState(
    val item: Item?,
    val groups: List<String>
)