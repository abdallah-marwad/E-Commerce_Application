package com.abdallah.ecommerce.utils.animation

import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication

object RecyclerAnimation {
    fun animateRecycler( rv : RecyclerView){
        val animation: LayoutAnimationController =
            AnimationUtils.loadLayoutAnimation(MyApplication.myAppContext, R.anim.recycler_layout_anmation)
        rv.layoutAnimation = animation
    }
}