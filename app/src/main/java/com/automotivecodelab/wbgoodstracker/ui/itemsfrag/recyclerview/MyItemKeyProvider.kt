package com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.SortedList
import com.automotivecodelab.wbgoodstracker.domain.models.Item

class MyItemKeyProvider() : ItemKeyProvider<String>(ItemKeyProvider.SCOPE_CACHED) {

    var sortedListItems: SortedList<Item>? = null
    var items: List<Item>? = null

    override fun getKey(position: Int) = sortedListItems?.get(position)?._id

    override fun getPosition(key: String): Int {

        val keyItem = items?.find { item -> item._id == key }

        return sortedListItems!!.indexOf(keyItem)
    }
}
