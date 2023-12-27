package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.BestDealsItemBinding
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.storage.StorageReference

class BestDealsAdapter(val data: ArrayList<Product> ) :
    RecyclerView.Adapter<BestDealsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BestDealsItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]

        Glide.with(holder.itemView.context)
                .load(data[position].productMainImg)
                .placeholder(R.drawable.err_banner)
                .error(R.drawable.err_banner)
                .into(holder.binding.bestDealsImg)
        val newPrice = item.price!! - item.offerValue!!
        holder.binding.itemOldPrice.paintFlags = holder.binding.itemOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        holder.binding.itemOldPrice.text = item.price.toString()
        holder.binding.offerPercentage.text = "${item.offerPercentage}% Off"
        holder.binding.itemNewPrice.text = "EGP "+newPrice
        holder.binding.itemName.text = item.productName


    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: BestDealsItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        var binding : BestDealsItemBinding
        init {
            binding = itemView
        }
    }
}
