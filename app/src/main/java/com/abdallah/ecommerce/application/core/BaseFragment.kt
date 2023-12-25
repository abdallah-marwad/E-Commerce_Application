package com.abdallah.ecommerce.application.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.databinding.FragmentBaseBinding
import com.abdallah.ecommerce.utils.BottomSheets.LoadingDialog
import com.matrix.tete.application.utils.extensions.gone
import com.matrix.tete.application.utils.extensions.visible
import java.lang.reflect.ParameterizedType

/**
 * Ahmed Elmokadim
 * elmokadim@gmail.com
 * 01/01/2020
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

  protected val binding by lazy { initBinding() }
  private val baseBinding by lazy { FragmentBaseBinding.inflate(layoutInflater) }
  private val loadingDialog by lazy { activity?.let { LoadingDialog(it) } }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    baseBinding.flContainer.addView(binding.root)
    return baseBinding.root
  }

  override fun onDestroy() {
    // Dismiss dialog before fragment destroyed otherwise IllegalArgumentException will arise.
    dismissLoading()
    super.onDestroy()
  }

  /**
   * Show loading dialog
   */
  fun showLoading() {
    loadingDialog?.show()
  }

  /**
   * Dismiss loading dialog
   */
  fun dismissLoading() {
    // Make sure that fragment is alive otherwise IllegalArgumentException will arise.
    if (isDetached.not()) loadingDialog?.dismiss()
  }

  protected fun showInnerLoading() = with(baseBinding.loadingView) { rlLoading.visible() }

  protected fun dismissInnerLoading() = with(baseBinding.loadingView) { rlLoading.gone() }


  protected fun showError(@DrawableRes drawable: Int = R.drawable.ic_connection,
                          message: String,
                          showRetry: Boolean = true,
                          action: String? = "Retry",
                          onRetry: () -> Unit) {
    with(baseBinding.errorView) {
      ivError.setImageResource(drawable)
      tvError.text = message
      if (showRetry) {
        btnRetry.visible()
        btnRetry.text = action
        btnRetry.setOnClickListener {
          onRetry.invoke()
          llError.gone()
        }
      }
      llError.visible()
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun initBinding(): VB {
    val superclass = javaClass.genericSuperclass as ParameterizedType
    val method = (superclass.actualTypeArguments[0] as Class<Any>)
        .getDeclaredMethod("inflate", LayoutInflater::class.java)
    return method.invoke(null, layoutInflater) as VB
  }

  protected fun showLongToast(msg : String){
    Toast.makeText(
      context,
      msg,
      Toast.LENGTH_LONG
    ).show()
  }
  protected fun showShortToast(msg : String) {
    Toast.makeText(
      context,
      msg,
      Toast.LENGTH_SHORT
    ).show()
  }
}