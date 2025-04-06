package com.automotivecodelab.wbgoodstracker.ui.itemsfrag

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.themeColor
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview.ItemsAdapter
import kotlin.math.abs

abstract class PartialSwipeCallback : ItemTouchHelper.SimpleCallback(
    /* dragDirs = */ 0,
    /* swipeDirs = */ ItemTouchHelper.LEFT
) {

    private var swipeBack = false

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (viewHolder is ItemsAdapter.ItemViewHolder)
            return super.getSwipeDirs(recyclerView, viewHolder)
        else
            0
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    abstract fun onSwipe(viewHolder: RecyclerView.ViewHolder)

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val card = (viewHolder as ItemsAdapter.ItemViewHolder).recyclerViewItemBinding
            .card
        c.clipRect(
            card.right + dX,
            card.top.toFloat(),
            card.right.toFloat(),
            card.bottom.toFloat()
        )
        val context = recyclerView.context
        val editIcon = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_baseline_delete_24,
            context.theme
        )
        if (editIcon != null) {
            editIcon.setTint(context.themeColor(com.google.android.material.R.attr.colorOnBackground))
            val rect = Rect(
                card.right - editIcon.intrinsicWidth - (
                        card.height - editIcon
                            .intrinsicHeight
                        ) / 4,
                card.top + (card.height - editIcon.intrinsicHeight) / 2,
                card.right - (card.height - editIcon.intrinsicHeight) / 4,
                card.top + editIcon.intrinsicHeight + (
                        card.height -
                                editIcon.intrinsicHeight
                        ) / 2
            )
            editIcon.bounds = rect
            // c.drawColor(Color.BLUE)
            editIcon.draw(c)
        }

        // setup swipe action trigger
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        super.onChildDraw(
            c, recyclerView, viewHolder, dX, dY, actionState,
            isCurrentlyActive
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { view: View?, event: MotionEvent ->
            // check if swipe gesture is finished
            swipeBack =
                event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                // check if swipe threshold is reached to trigger callback
                val threshold = calculateSwipeActionThreshold(viewHolder)
                if (abs(dX) > threshold) onSwipe(viewHolder)

                // call super to draw view like before swipe started
                super@PartialSwipeCallback.onChildDraw(
                    canvas,
                    recyclerView,
                    viewHolder,
                    0f,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
            false
        }
    }

    private fun calculateSwipeActionThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        // swipe 1/2 of the view width to trigger the action
        return (viewHolder.itemView.width / 2).toFloat()
    }
}