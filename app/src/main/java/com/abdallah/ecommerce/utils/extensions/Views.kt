package com.matrix.tete.application.utils.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Orientation
import androidx.recyclerview.widget.RecyclerView.VERTICAL

/**
 * Ahmed Ali Ebaid
 * ahmedali26002844@gmail.com
 * 14/06/2023
 */

/**
 * Set view visibility visible
 */
fun View.visible() {
  visibility = VISIBLE
}

/**
 * Set view visibility invisible
 */
fun View.invisible() {
  visibility = INVISIBLE
}

/**
 * Set view visibility gone
 */
fun View.gone() {
  visibility = GONE
}

/**
 * Avoid boilerplate code when inflating layout
 * @param layout Layout resource
 * @return Inflated view
 */
fun ViewGroup.inflate(@LayoutRes layout: Int) = LayoutInflater.from(context).inflate(layout, this, false)

/**
 * Set LinearLayoutManager to RecyclerView
 * @param orientation (VERTICAL or HORIZONTAL)
 */
fun RecyclerView.linearLayoutManager(@Orientation orientation: Int = VERTICAL) {
  layoutManager = LinearLayoutManager(this.context, orientation, false)
}