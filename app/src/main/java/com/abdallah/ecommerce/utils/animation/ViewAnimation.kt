package com.abdallah.ecommerce.utils.animation

import android.view.View
import android.view.ViewGroup
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager

class ViewAnimation {
     fun viewAnimation(view : View, viewGroup: ViewGroup) {
        val fadeTransition: Transition = Fade()
        fadeTransition.duration = 600
        fadeTransition.addTarget(view)
        TransitionManager.beginDelayedTransition(viewGroup, fadeTransition)
    }
}