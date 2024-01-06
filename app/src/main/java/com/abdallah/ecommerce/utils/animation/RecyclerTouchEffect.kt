package com.abdallah.ecommerce.utils.animation

import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

 class RecyclerTouchEffect() : OnTouchListener {


        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN ->
                    scaleView(
                        view,
                         0.9f,
                         0.9f,
                    )


                MotionEvent.ACTION_UP ->
                    scaleView(
                        view, 1.0.toFloat(), 1.0.toFloat(),

                    )


                MotionEvent.ACTION_CANCEL -> scaleView(
                    view, 1.0.toFloat(),
                    1.0.toFloat(),

                )
            }
            return false
        }

    private fun scaleView(
        v: View,
        startScale: Float,
        endScale: Float,
    ) {
        v.setOnTouchListener(RecyclerTouchEffect())
        val anim: Animation = ScaleAnimation(
            startScale,
            endScale,
            startScale,
            endScale,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        anim.fillAfter = true
        anim.duration = 100
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {

                v.setOnTouchListener(RecyclerTouchEffect())
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        v.startAnimation(anim)
    }


}

