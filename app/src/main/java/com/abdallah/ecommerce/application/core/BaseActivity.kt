package com.abdallah.ecommerce.application.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.databinding.ActivityBaseBinding
import com.abdallah.ecommerce.utils.BottomSheets.LoadingDialog
import com.abdallah.ecommerce.utils.LocaleHelper
import com.google.android.material.internal.ViewUtils.hideKeyboard
import java.lang.reflect.ParameterizedType


/**
 * Ahmed Elmokadim
 * elmokadim@gmail.com
 * 01/01/2020
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

  protected val binding by lazy { initBinding() }
  private val baseBinding by lazy { ActivityBaseBinding.inflate(layoutInflater) }
  private val loadingDialog by lazy { LoadingDialog(this) }

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(LocaleHelper.onAttach(newBase))
  }

  @Suppress("DEPRECATION")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Set the status bar to dark-semi-transparent
    window.setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS)
    setContent()
  }

  @Suppress("UNCHECKED_CAST")
  private fun initBinding(): VB {
    val superclass = javaClass.genericSuperclass as ParameterizedType
    val method = (superclass.actualTypeArguments[0] as Class<Any>)
        .getDeclaredMethod("inflate", LayoutInflater::class.java)
    return method.invoke(null, layoutInflater) as VB
  }

  private fun setContent() {
    baseBinding.flContainer.addView(binding.root)
    setContentView(baseBinding.root)
  }

  @SuppressLint("RestrictedApi")
  override fun onPause() {
    currentFocus?.let { hideKeyboard(it) }
    super.onPause()
  }

  /**
   * Show loading dialog
   */
  fun showLoading() = loadingDialog.show()

  /**
   * Dismiss loading dialog
   */
  fun dismissLoading() {
    // Make sure that activity is alive otherwise IllegalArgumentException will arise.
    if (isDestroyed.not()) loadingDialog.dismiss()
  }

  /**
   * Show inner loading view
   */
  protected fun showInnerLoading() = with(baseBinding.loadingView) { rlLoading.visibility = View.VISIBLE }

  /**
   * Dismiss inner loading view
   */
  protected fun dismissInnerLoading() = with(baseBinding.loadingView) { rlLoading.visibility = View.GONE }

  protected fun showError(@DrawableRes drawable: Int = R.drawable.ic_cart,
                          message: String,
                          showRetry: Boolean = true,
                          action: String? = getString(R.string.facebook),
                          onRetry: () -> Unit) {
    with(baseBinding.errorView) {
      ivError.setImageResource(drawable)
      tvError.text = message
      if (showRetry) {
        btnRetry.visibility = View.VISIBLE
        btnRetry.text = action
        btnRetry.setOnClickListener {
          onRetry.invoke()
          llError.visibility = View.GONE
        }
      }
      llError.visibility = View.GONE
    }
  }
}