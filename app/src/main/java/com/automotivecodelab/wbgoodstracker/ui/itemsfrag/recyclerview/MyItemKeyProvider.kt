package com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.SortedList
import com.automotivecodelab.wbgoodstracker.domain.models.Item

class MyItemKeyProvider : ItemKeyProvider<String>(SCOPE_CACHED) {

    var sortedListItems: SortedList<Item>? = null

    override fun getKey(position: Int) = sortedListItems?.get(position)?.id

    override fun getPosition(key: String): Int {
        var index = -1
        for (i in 0 until (sortedListItems?.size() ?: 0)) {
            val item = sortedListItems?.get(i)
            if (item?.id == key) {
                index = i
                break
            }
        }
        return index
    }
}
