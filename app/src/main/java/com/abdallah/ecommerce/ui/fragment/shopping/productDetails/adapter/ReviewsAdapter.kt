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
import com.abdallah.ecommerce.data.model.RatingModel
import com.abdallah.ecommerce.data.model.SizesModel
import com.abdallah.ecommerce.databinding.ColorsAndSizesBinding
import com.abdallah.ecommerce.databinding.RatingCommentItemBinding
import com.abdallah.ecommerce.utils.animation.RecyclerTouchEffect

class ReviewsAdapter( var list: ArrayList<RatingModel>?) : RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RatingCommentItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list!![position]
        holder.binding.reviewerName.text = item.raterName
        if(item.comment == "")
            holder.binding.revieweTxt.visibility = View.GONE
        holder.binding.itemRatingBar.rating = item.rating
        holder.binding.revieweTxt.text = item.comment
        holder.binding.dateTxt.text = item.date

    }


    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    inner class ViewHolder(val binding: RatingCommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}
