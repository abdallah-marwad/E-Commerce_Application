package com.matrix.tete.application.utils.extensions

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * Ahmed Ali Ebaid
 * ahmedali26002844@gmail.com
 * 14/06/2023
 */

/**
 * Dismiss keyboards
 * @param view View of current focus
 */
fun Context.hideKeyboard(view: View?) = view?.let {
  val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
  if (imm.isActive) {
    imm.hideSoftInputFromWindow(it.windowToken, 0)
  }
}

/**
 * Get color from resources
 * @param colorId Id of color which will load
 * @return Chosen color
 */
fun Context.getColorCompat(@ColorRes colorId: Int): Int {
  return ContextCompat.getColor(this, colorId)
}

/**
 * Get drawable from resources
 * @param drawableId Id of drawable which will load
 * @return Chosen drawable
 */
fun Context.getDrawableCompat(@DrawableRes drawableId: Int): Drawable? {
  return ContextCompat.getDrawable(this, drawableId)
}