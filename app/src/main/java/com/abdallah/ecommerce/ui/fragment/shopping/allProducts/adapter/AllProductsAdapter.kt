package com.abdallah.ecommerce.ui.fragment.shopping.allProducts.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.AllProductsItemBinding
import com.abdallah.ecommerce.databinding.BestDealsItemBinding
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.abdallah.ecommerce.utils.animation.RecyclerTouchEffect
import com.bumptech.glide.Glide

class AllProductsAdapter(val data: ArrayList<Product> ) :
    RecyclerView.Adapter<AllProductsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AllProductsItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]

        Glide.with(holder.itemView.context)
                .load(item.productMainImg)
                .placeholder(CustomShimmerDrawable().shimmerDrawable)
                .error(R.drawable.err_banner)
                .into(holder.binding.productImage)
        val newPrice = item.price!! - item.offerValue!!
        holder.binding.itemOldPrice.paintFlags = holder.binding.itemOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        holder.binding.itemOldPrice.text = item.price.toString()
        holder.binding.offerPercentage.text = "${item.offerPercentage}% Off"
        holder.binding.itemNewPrice.text = "EGP "+newPrice
        holder.binding.itemName.text = item.productName
        holder.binding.ratingNumber.text ="(${item.ratersNum})"
        holder.binding.itemRatingBar.rating = item.rating
        holder.binding.parentArea.setOnClickListener {

        }
        holder.binding.parentArea.setOnTouchListener(RecyclerTouchEffect())



    }


    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder( var binding : AllProductsItemBinding) : RecyclerView.ViewHolder(binding.root)
}
