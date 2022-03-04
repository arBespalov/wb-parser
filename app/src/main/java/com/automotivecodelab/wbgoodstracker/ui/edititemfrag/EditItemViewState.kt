package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import com.automotivecodelab.wbgoodstracker.domain.models.Item

data class EditItemViewState(
    val item: Item?,
    val groups: Array<String>
) {
    // auto-generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EditItemViewState

        if (item != other.item) return false
        if (!groups.contentEquals(other.groups)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = item.hashCode()
        result = 31 * result + groups.contentHashCode()
        return result
    }
}
