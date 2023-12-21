package com.abdallah.ecommerce.utils.dialogs

import android.app.Activity
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.view.Gravity.CENTER
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window.FEATURE_NO_TITLE
import androidx.appcompat.app.AppCompatDialog
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.databinding.DialogLoadingBinding
import com.race604.drawable.wave.WaveDrawable

/**
 * Ahmed Ali Ebaid
 * ahmedali26002844@gmail.com
 * 14/06/2023
 */
class LoadingDialog(context: Activity) : AppCompatDialog(context , R.style.LoadingTheme) {

  private val binding = DialogLoadingBinding.inflate(layoutInflater)

  init {
    window?.apply {
      setLayout(WRAP_CONTENT, WRAP_CONTENT)
      setBackgroundDrawable(ColorDrawable(TRANSPARENT))
      setGravity(CENTER)
    }
    requestWindowFeature(FEATURE_NO_TITLE)
    setContentView(binding.root)
    setCancelable(false)

    val drawable = WaveDrawable(getContext(), R.drawable.ic_kleine_shape)
    binding.ivLoading.setImageDrawable(drawable)
    drawable.isIndeterminate = true
    drawable.setWaveAmplitude(30)
    drawable.setWaveLength(70)
    drawable.setWaveSpeed(65)
  }
}