package com.abdallah.ecommerce.utils.animation

import android.graphics.Canvas
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.OnSwipe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.ui.fragment.shopping.cart.CartRVAdapter
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class RvSwipe {
     fun onSwipe(onSwipe:  (RecyclerView.ViewHolder, Int) -> Unit): ItemTouchHelper.SimpleCallback {
        val itemTouchHelper = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onSwipe(viewHolder , direction)
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
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addActionIcon(R.drawable.baseline_delete_outline_24)
                    .create()
                    .decorate()
                super.onChildDraw(
                    c!!, recyclerView!!,
                    viewHolder!!, dX, dY, actionState, isCurrentlyActive
                )
            }

        }
        return itemTouchHelper
    }

}