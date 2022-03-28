package com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class MyItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
    override fun getItemDetails(e: MotionEvent) = recyclerView.findChildViewUnder(e.x, e.y)
        ?.let {
            (recyclerView.getChildViewHolder(it) as? ViewHolderWithDetails<String>)?.getItemDetail()
        }
}
