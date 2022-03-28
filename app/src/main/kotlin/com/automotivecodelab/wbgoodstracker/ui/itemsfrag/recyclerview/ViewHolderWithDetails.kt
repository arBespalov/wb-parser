package com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview

import androidx.recyclerview.selection.ItemDetailsLookup

interface ViewHolderWithDetails<TItem> {
    fun getItemDetail(): ItemDetailsLookup.ItemDetails<TItem>
}

class MyItemDetails(
    private val adapterPosition: Int,
    private val selectedKey: String?
) : ItemDetailsLookup.ItemDetails<String>() {

    override fun getSelectionKey() = selectedKey

    override fun getPosition() = adapterPosition
}
