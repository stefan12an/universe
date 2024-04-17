package com.stefan.universe.common.views

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.stefan.universe.ui.home.data.adapter.ChatListAdapter
import kotlin.math.roundToInt

fun setItemTouchHelper(
    context: Context,
    recyclerView: RecyclerView,
    adapter: ChatListAdapter
) {

    ItemTouchHelper(object : ItemTouchHelper.Callback() {

        private val limitScrollX = dipToPx(context)
        private var currentScrollX = 0
        var leftSwipeChecker = false

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val dragFlags = 0
            val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return Integer.MAX_VALUE.toFloat()
        }

        override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
            return Integer.MAX_VALUE.toFloat()
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                leftSwipeChecker = viewHolder.itemView.scrollX == 0 && dX < 0

                if (leftSwipeChecker) {
                    recoverSwipedItem(viewHolder, recyclerView)
                    leftSwipeChecker = viewHolder.itemView.scrollX != 0
                }

                if (isCurrentlyActive) {
                    val scrollOffset = (currentScrollX - dX).coerceIn(0f, limitScrollX.toFloat())
                    viewHolder.itemView.scrollTo(scrollOffset.roundToInt(), 0)
                } else if (dX == 0f) {
                    currentScrollX = viewHolder.itemView.scrollX
                }
            }
        }

        private fun recoverSwipedItem(
            viewHolder: RecyclerView.ViewHolder,
            recyclerView: RecyclerView
        ) {
            for (i in adapter.itemCount downTo 0) {
                val itemView = recyclerView.findViewHolderForAdapterPosition(i)?.itemView
                itemView?.scrollTo(0, 0)
            }
        }

        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)

            if (viewHolder.itemView.scrollX > limitScrollX / 2) {
                viewHolder.itemView.scrollTo(limitScrollX, 0)
            } else {
                viewHolder.itemView.scrollTo(0, 0)
            }
        }

    }).apply {
        attachToRecyclerView(recyclerView)
    }
}

private fun dipToPx(context: Context): Int {
    return (100f * context.resources.displayMetrics.density).toInt()
}