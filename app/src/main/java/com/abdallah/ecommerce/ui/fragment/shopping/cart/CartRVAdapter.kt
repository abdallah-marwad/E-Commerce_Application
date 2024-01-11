package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.BestDealsItemBinding
import com.abdallah.ecommerce.databinding.CartProductItemBinding
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.BestDealsAdapter
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.abdallah.ecommerce.utils.animation.RecyclerTouchEffect
import com.bumptech.glide.Glide

class CartRVAdapter(val data: MutableList<CartProduct> , val listener: CartOnClick) :
    RecyclerView.Adapter<CartRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CartProductItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        Glide.with(holder.itemView.context)
            .load(item.product.productMainImg)
            .placeholder(CustomShimmerDrawable().shimmerDrawable)
            .error(R.drawable.err_banner)
            .into( holder.binding.imageCartProduct)
        holder.binding.tvProductCartName.text = item.product.productName
        holder.binding.imageCartProductColor.setImageDrawable(
            ColorDrawable(item.color))
        holder.binding.tvCartProductSize.text = item.size
        holder.binding.tvCartProductQuantity.text = item.quantity.toString()
        holder.binding.tvProductCartPrice.text = item.product.price.toString()
        holder.binding.imagePlus.setOnClickListener{
//            listener.plusOnClick()
        }
        holder.binding.imageMinus.setOnClickListener{}
        holder.binding.cardParent.setOnClickListener{}

    }

    interface CartOnClick {
        fun itemOnClick(product: CartProduct, view: View)
        fun plusOnClick(product: CartProduct, view: View)
        fun minusOnClick(product: CartProduct, view: View)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}
