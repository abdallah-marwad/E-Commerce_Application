package com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication

import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.ColorModel
import com.abdallah.ecommerce.data.model.SizesModel
import com.abdallah.ecommerce.databinding.ColorsAndSizesBinding
import com.abdallah.ecommerce.utils.animation.RecyclerTouchEffect

class ColorsAdapter(val dataColors: ArrayList<ColorModel>?, val dataSizes: ArrayList<SizesModel>?) :
    RecyclerView.Adapter<ColorsAdapter.ViewHolder>() {
    var selectedColors = ArrayList<Int>()
        private set
    var selectedSizes = ArrayList<String>()
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ColorsAndSizesBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (dataColors != null) {
            val item = dataColors[position]

            holder.binding.parentArea1.setCardBackgroundColor(item.color)
            if (item!!.isSelected) {
                holder.binding.imgDone.visibility = View.VISIBLE
            } else {
                holder.binding.imgDone.visibility = View.GONE
            }
            holder.binding.parentArea.setOnClickListener {
                if (item!!.isSelected) {
                    item.isSelected = false
                    selectedColors.remove(item.color)
                    notifyDataSetChanged()
                    return@setOnClickListener
                }
                selectedColors.add(item.color)
                item.isSelected = true
                notifyDataSetChanged()
            }

        } else {
            val item = dataSizes!!.get(position)
            if (item!!.isSelected) {
                holder.binding.imgDone.visibility = View.VISIBLE
            } else {
                holder.binding.imgDone.visibility = View.GONE
            }
            holder.binding.parentArea.setOnClickListener {
                if (item!!.isSelected) {
                    item.isSelected = false
                    selectedSizes.remove(item.size)
                    notifyDataSetChanged()
                    return@setOnClickListener
                }
                selectedSizes.add(item.size)
                item.isSelected = true
                notifyDataSetChanged()
            }
            holder.binding.parentArea1.setCardBackgroundColor(
                MyApplication.myAppContext.resources.getColor(
                    R.color.g_gray700
                )
            )
            holder.binding.tvSize.text = dataSizes?.get(position)?.size ?: ""
            holder.binding.tvSize.visibility = View.VISIBLE
        }


        holder.binding.parentArea.setOnTouchListener(RecyclerTouchEffect())


    }

    fun removeSelectedItem() {
        dataColors!!.forEach {
            it.isSelected = false
        }
    }

    override fun getItemCount(): Int {
        return dataColors?.size ?: dataSizes!!.size
    }

    inner class ViewHolder(val binding: ColorsAndSizesBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}
