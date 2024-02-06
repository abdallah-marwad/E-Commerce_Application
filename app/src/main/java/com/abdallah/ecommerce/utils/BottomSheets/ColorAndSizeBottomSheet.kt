package com.abdallah.ecommerce.utils.BottomSheets

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.data.model.ColorModel
import com.abdallah.ecommerce.data.model.SizesModel
import com.abdallah.ecommerce.databinding.ColorsAndSizesBinding
import com.abdallah.ecommerce.databinding.ColorsSizesBottomSheetBinding
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter.ColorsAdapter
import com.abdallah.ecommerce.utils.ColorAndSizeConverter
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.wang.avi.AVLoadingIndicatorView

class ColorAndSizeBottomSheet : ColorsAdapter.SelectedColorAndSize {
    var dialog: BottomSheetDialog? = null
    lateinit var binding: ColorsSizesBottomSheetBinding
    var selectedColor = -2
    var selectedSize : String? = null
    fun showDialog(
        dataColors: ArrayList<Int>?,
        dataSizes: ArrayList<String>?,
        onSendClick: ((selectedSize:String?, selectedColor:Int) -> Unit)? = null
    ) {
        dialog =
            BottomSheetDialog(MyApplication.myAppContext.getCurrentAct()!!, R.style.DialogStyle)
        val view = MyApplication.myAppContext.getCurrentAct()!!.layoutInflater.inflate(
            R.layout.colors_sizes_bottom_sheet,
            null
        )
        binding = ColorsSizesBottomSheetBinding.bind(view)
        dialog!!.setContentView(binding.root)
        dialog!!.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog!!.window?.findViewById<View>(R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)
        dialog!!.show()

        dialog!!.apply {
            setOnShowListener {
                val bottomSheet =
                    findViewById<View?>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.setBackgroundResource(android.R.color.transparent)
            }
        }

        initSizesRv(dataSizes)
        initColorsRv(dataColors!!)
        binding.btnDone.setOnClickListener {
            val validateSizes = dataSizes == null
            if (isDataSelected(validateSizes).not()) {
                return@setOnClickListener
            }
            if (onSendClick != null) {
            onSendClick(selectedSize,selectedColor )
            }
        }

    }

    private fun isDataSelected(validateSize : Boolean): Boolean {
        var isSelected = true
        if (selectedColor == -2) {
            binding.tvColorError.visibility = View.VISIBLE
            isSelected = false
        }
        if (validateSize &&selectedSize == null) {
            binding.tvSizeError.visibility = View.VISIBLE
            isSelected = false
        }
        return isSelected
    }

    fun dismiss() {
        if (dialog != null && dialog!!.isShowing)
            dialog?.dismiss()
    }
    private fun initColorsRv(dataColors: ArrayList<Int>) {
        val colorModelList = ColorAndSizeConverter().toColorList(dataColors)
        val colorsAdapter = ColorsAdapter(colorModelList, null, this)
        binding.rvColors.adapter = colorsAdapter
        RecyclerAnimation.animateRecycler(binding.rvColors)
        colorsAdapter.notifyDataSetChanged()
        binding.rvColors.scheduleLayoutAnimation()
    }

    private fun initSizesRv(dataSizes : ArrayList<String>?) {
        val sizesList = dataSizes ?: ArrayList()
        val sizesModelList = ColorAndSizeConverter().toSizeList(sizesList)

        if (sizesModelList.isEmpty()) {
            selectedSize = null
            binding.sizesTxt.visibility = View.GONE
            binding.rvSizes.visibility = View.GONE
            return
        }
        val sizesAdapter = ColorsAdapter(null, sizesModelList, this)
        binding.rvSizes.adapter = sizesAdapter
        RecyclerAnimation.animateRecycler(binding.rvSizes)
        sizesAdapter.notifyDataSetChanged()
        binding.rvSizes.scheduleLayoutAnimation()

    }

    override fun selectedColor(color: Int) {
        selectedColor = color
    }

    override fun selectedSize(size: String) {
        selectedSize = size
    }
}
