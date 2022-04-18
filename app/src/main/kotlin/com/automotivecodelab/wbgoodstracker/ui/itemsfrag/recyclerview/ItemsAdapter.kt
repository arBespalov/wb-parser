package com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.databinding.RecyclerviewItemBinding
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.httpToHttps
import com.automotivecodelab.wbgoodstracker.themeColor
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Picasso
import timber.log.Timber

class ItemsAdapter(
    private var comparator: Comparator<Item>?,
    private val onOpenItemDetails: (recyclerItemPosition: Int) -> Unit,
) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    private val ITEM_CONTENT_CHANGED_PAYLOAD = "itemContentChangedPayload"
    private val defaultTextViewColors = mutableMapOf<String, ColorStateList>()

    var tracker: SelectionTracker<String>? = null
        private set

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        tracker = SelectionTracker.Builder(
            "id",
            recyclerView,
            MyItemKeyProvider(sortedList),
            MyItemDetailsLookup(recyclerView),
            StorageStrategy.createStringStorage()
        ).build()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        tracker = null
    }

    fun setItemsComparator(comparator: Comparator<Item>) {
        this.comparator = comparator
        sortedList.beginBatchedUpdates()
        var isItemsInWrongPlaces = true
        while (isItemsInWrongPlaces) {
            for (position in 0 until sortedList.size())
                sortedList.recalculatePositionOfItemAt(position)
            isItemsInWrongPlaces = false
            for (position in 1 until sortedList.size()) {
                if (comparator.compare(sortedList[position], sortedList[position - 1]) > 0) {
                    isItemsInWrongPlaces = false
                } else {
                    isItemsInWrongPlaces = true
                    break
                }
            }
        }
        sortedList.endBatchedUpdates()
    }

    private val sortedList = SortedList(
        Item::class.java,
        object : SortedList.Callback<Item>() {
            override fun getChangePayload(item1: Item?, item2: Item?): Any {
                return ITEM_CONTENT_CHANGED_PAYLOAD
            }
            override fun areItemsTheSame(item1: Item?, item2: Item?): Boolean {
                return item1?.id == item2?.id
            }
            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }
            override fun onChanged(position: Int, count: Int) {
                notifyItemRangeChanged(position, count)
            }
            override fun onChanged(position: Int, count: Int, payload: Any?) {
                notifyItemRangeChanged(position, count, payload)
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
                return (oldItem != null && newItem != null &&
                        oldItem.localName == newItem.localName &&
                        oldItem.name == newItem.name &&
                        oldItem.averagePrice == newItem.averagePrice &&
                        oldItem.averagePriceDelta == newItem.averagePriceDelta &&
                        oldItem.ordersCount == newItem.ordersCount &&
                        oldItem.ordersCountDelta == newItem.ordersCountDelta &&
                        oldItem.averageOrdersCountPerDay == newItem.averageOrdersCountPerDay &&
                        oldItem.totalQuantity == newItem.totalQuantity &&
                        oldItem.totalQuantityDelta == newItem.totalQuantityDelta &&
                        isUpdateIndicatorVisible(oldItem) == isUpdateIndicatorVisible(newItem))
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

    fun replaceAll(items: List<Item>) {
        // replaceAll() do not triggers onMove callback: it goes like onRemoved-onInsert, so
        // animation suffers. in case of updating item in sorted list one by one - onMoved is
        // triggered, but we need to check if the items in the new list are the same as items in
        // existing list. for example, when signed-in user delete item on one device and then
        // sync another device
        val currentList = mutableListOf<Item>()
        for (index in 0 until sortedList.size()) {
            currentList.add(sortedList.get(index))
        }
        val currentItemsId = currentList.map { it.id }.sorted().toTypedArray()
        val newItemsId = items.map { it.id }.sorted().toTypedArray()
        if (!currentItemsId.contentEquals(newItemsId)) {
            Timber.d("replace all")
            sortedList.replaceAll(items)
            return
        }
        sortedList.beginBatchedUpdates()
        Timber.d("update one by one")
        for (index in 0 until sortedList.size()) {
            sortedList.updateItemAt(
                index,
                items.find { it.id == sortedList[index].id }
            )
        }
        sortedList.endBatchedUpdates()
    }

    inner class ItemViewHolder(val recyclerViewItemBinding: RecyclerviewItemBinding) :
        RecyclerView.ViewHolder(recyclerViewItemBinding.root), ViewHolderWithDetails<String> {
        init {
            recyclerViewItemBinding.card.setOnClickListener {
                onOpenItemDetails(bindingAdapterPosition)
            }
        }
        override fun getItemDetail() = MyItemDetails(
            bindingAdapterPosition,
            sortedList.get(bindingAdapterPosition).id
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.recyclerview_item,
            parent,
            false
        )
    )

    override fun getItemCount() = sortedList.size()

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) return super.onBindViewHolder(holder, position, payloads)
        val item = sortedList[position]
        for (payload in payloads) {
            when (payload) {
                ITEM_CONTENT_CHANGED_PAYLOAD -> {
                    val oldCount = holder.recyclerViewItemBinding.totalQuantity.text.toString()
                        .toInt()
                    val newCount = item.totalQuantity
                    if (oldCount != newCount) {
                        holder.recyclerViewItemBinding.item = item.copy(totalQuantity = oldCount)
                        ValueAnimator.ofInt(oldCount, newCount).apply {
                            duration = 200
                            addUpdateListener {
                                holder.recyclerViewItemBinding.totalQuantity.text =
                                    it.animatedValue.toString()
                            }
                            start()
                        }

                    } else {
                        holder.recyclerViewItemBinding.item = item
                    }
                    holder.recyclerViewItemBinding.updateIndicator.visibility =
                        if (isUpdateIndicatorVisible(item)) View.VISIBLE else View.INVISIBLE
                }
                else -> super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    @SuppressLint("PrivateResource")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = sortedList[position]
        holder.recyclerViewItemBinding.item = item
        holder.recyclerViewItemBinding.updateIndicator.visibility =
            if (isUpdateIndicatorVisible(item)) View.VISIBLE else View.INVISIBLE
        val loadImage = {
            Picasso.get()
                .load(item.img.httpToHttps())
                .fit()
                .centerCrop()
                .error(R.drawable.ic_baseline_error_outline_24)
                .into(holder.recyclerViewItemBinding.imageView)
        }
        loadImage()
        holder.recyclerViewItemBinding.imageView.addOnLayoutChangeListener {
                view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val height = bottom - top
            val oldHeight = oldBottom - oldTop
            if (oldHeight != 0 && height != oldHeight && position < sortedList.size()) {
                loadImage()
            }
        }
        tracker?.let { selectionTracker ->
            val context = holder.itemView.context
            val backgroundColor = if (selectionTracker.isSelected(item.id))
                context.themeColor(R.attr.colorSecondary)
            else
                // colorSurfaceVariant - default material3 card background
                context.themeColor(R.attr.colorSurfaceVariant)
            holder.recyclerViewItemBinding.card.setCardBackgroundColor(backgroundColor)

            val indicatorColor = if (selectionTracker.isSelected(item.id))
                context.themeColor(R.attr.colorOnSecondary)
            else
                ContextCompat.getColor(context, R.color.red)
            holder.recyclerViewItemBinding.updateIndicator.background.setTint(indicatorColor)

            if (defaultTextViewColors.isEmpty()) {
                holder.itemView.allViews.forEach { view ->
                    if (view is MaterialTextView) {
                        defaultTextViewColors[context.resources.getResourceEntryName(view.id)] =
                            view.textColors
                    }
                }
            }

            holder.itemView.allViews.forEach { view ->
                if (view is MaterialTextView) {
                    if (selectionTracker.isSelected(item.id)) {
                        view.setTextColor(context.themeColor(R.attr.colorOnSecondary))
                    } else {
                        val colorStateList = defaultTextViewColors[
                                context.resources.getResourceEntryName(view.id)]
                        if (colorStateList != null) {
                            view.setTextColor(colorStateList)
                        }
                    }
                }
            }
        }
    }

    private fun isUpdateIndicatorVisible(item: Item) =
        (item.totalQuantityDelta == 0 && item.sizes.any { it.quantityDelta != 0 }) ||
                item.feedbacksDelta != 0
}