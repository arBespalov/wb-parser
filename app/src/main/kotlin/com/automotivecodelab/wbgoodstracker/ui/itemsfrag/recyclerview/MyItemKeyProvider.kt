package com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.automotivecodelab.wbgoodstracker.domain.models.Item

class MyItemKeyProvider(
    private val sortedListItems: SortedList<Item>?,
    private val recyclerView: RecyclerView
): ItemKeyProvider<String>(SCOPE_CACHED) {

    override fun getKey(position: Int): String? {
        val vh = recyclerView.findViewHolderForAdapterPosition(position) as?
                ItemsAdapter.ItemViewHolder
        return vh?.recyclerViewItemBinding?.item?.id
    }

    override fun getPosition(key: String): Int {
        var index: Int? = null
        for (i in 0 until (sortedListItems?.size() ?: 0)) {
            val item = sortedListItems?.get(i)
            if (item?.id == key) {
                index = i
                break
            }
        }
        return if (index == null) {
            RecyclerView.NO_POSITION
        } else {
            // for handling case when header exists
            if (getKey(index) != key) {
                index += 1
            }
            index
        }
    }
}
