package com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.databinding.RecyclerviewItemBinding
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.httpToHttps
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.ItemsViewModel
import com.google.android.material.color.MaterialColors
import com.squareup.picasso.Picasso

class ItemsAdapter(
    private val viewModel: ItemsViewModel,
    var comparator: Comparator<Item>?
) : RecyclerView.Adapter<ItemsAdapter.ItemsViewHolder>() {

    var tracker: SelectionTracker<String>? = null

    val sortedList = SortedList(Item::class.java, object : SortedList.Callback<Item>() {
            override fun areItemsTheSame(item1: Item?, item2: Item?): Boolean {
                return item1?.id == item2?.id
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemRangeChanged(position, count)
            }

            override fun onInserted(position: Int, count: Int) {
                notifyItemRangeInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRangeRemoved(position, count)
            }

            override fun compare(o1: Item?, o2: Item?): Int {
                return comparator?.compare(o1, o2) ?: 0
            }

            override fun areContentsTheSame(oldItem: Item?, newItem: Item?): Boolean {
                return (
                    oldItem != null && newItem != null &&
                        oldItem.localName == newItem.localName &&
                        oldItem.name == newItem.name &&
                        oldItem.averagePrice == newItem.averagePrice &&
                        oldItem.averagePriceDelta == newItem.averagePriceDelta &&
                        oldItem.ordersCount == newItem.ordersCount &&
                        oldItem.ordersCountDelta == newItem.ordersCountDelta &&
                        oldItem.averageOrdersCountPerDay == newItem.averageOrdersCountPerDay &&
                        oldItem.totalQuantity == newItem.totalQuantity &&
                        oldItem.totalQuantityDelta == newItem.totalQuantityDelta
                    )
            }
        }
    )

    fun add(item: Item) {
        sortedList.add(item)
    }

    fun add(items: List<Item>) {
        sortedList.addAll(items)
    }

    fun remove(item: Item) {
        sortedList.remove(item)
    }

    fun remove(items: List<Item>) {
        sortedList.beginBatchedUpdates()
        items.forEach { sortedList.remove(it) }
        sortedList.endBatchedUpdates()
    }

    fun replaceAll(items: List<Item>) {
        sortedList.replaceAll(items)
    }

    inner class ItemsViewHolder(val recyclerViewItemBinding: RecyclerviewItemBinding) :
        RecyclerView.ViewHolder(recyclerViewItemBinding.root), ViewHolderWithDetails<String> {

        init {
            this.recyclerViewItemBinding.card.setOnClickListener {
                viewModel.openItem(bindingAdapterPosition)
            }
        }

        override fun getItemDetail() = MyItemDetails(
            bindingAdapterPosition,
            sortedList.get(bindingAdapterPosition).id
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemsViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.recyclerview_item,
            parent,
            false
        )
    )

    override fun getItemCount() = sortedList.size()

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {
        holder.recyclerViewItemBinding.apply {
            item = sortedList[position]
        }
        val imgUrl = httpToHttps(sortedList[position].img)
        holder.recyclerViewItemBinding.card.postDelayed(50) {
            Picasso.get()
                .load(imgUrl)
                .fit()
                .centerCrop()
                .error(R.drawable.ic_baseline_error_outline_24)
                .into(holder.recyclerViewItemBinding.imageView)
        }
        tracker?.let {
            if (it.isSelected(sortedList[position].id)) {
                holder.recyclerViewItemBinding.card.setCardBackgroundColor(
                    MaterialColors.getColor(
                        holder.recyclerViewItemBinding.root,
                        R.attr.colorSecondary
                    )
                )
                // holder.recyclerViewItemBinding.root.isActivated = true
            } else {
                holder.recyclerViewItemBinding.card.setCardBackgroundColor(
                    MaterialColors.getColor(
                        holder.recyclerViewItemBinding.root,
                        R.attr.colorSurface
                    )
                )
                // holder.recyclerViewItemBinding.root.isActivated = false
            }
        }
    }
}
