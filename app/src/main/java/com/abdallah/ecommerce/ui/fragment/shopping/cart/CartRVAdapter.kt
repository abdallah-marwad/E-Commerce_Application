package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.databinding.CartProductItemBinding
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.bumptech.glide.Glide

class CartRVAdapter(val data: MutableList<CartProduct>, val listener: CartOnClick) :
    RecyclerView.Adapter<CartRVAdapter.ViewHolder>() {

    var totalPrice = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CartProductItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        val newPrice = item.product.price!! - item.product.offerValue!!
        holder.initViews( item, newPrice)
        holder.handleClicks( item, newPrice, holder.layoutPosition)

    }


    interface CartOnClick {
        fun itemOnClick(product: CartProduct, view: View)
        fun plusOnClick(product: CartProduct, position: Int, price: Double, isChecked: Boolean)
        fun minusOnClick(product: CartProduct ,  position: Int, price: Double, isChecked: Boolean)
        fun cartCheckBox(totalPrice: Double)
    }


    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun initViews( item: CartProduct, newPrice: Double) {
            Glide.with(itemView.context)
                .load(item.product.productMainImg)
                .placeholder(CustomShimmerDrawable().shimmerDrawable)
                .error(R.drawable.err_banner)
                .into(binding.imageCartProduct)
            binding.tvProductCartName.text = item.product.productName
            binding.imageCartProductColor.setImageDrawable(
                ColorDrawable(item.color)
            )
            binding.tvCartProductSize.text = item.size
            binding.tvCartProductQuantity.text = item.quantity.toString()
            binding.tvProductCartPrice.text = newPrice.toString()
            binding.checkBox.isChecked = item.isChecked
        }

        fun handleClicks(
            item: CartProduct,
            newPrice: Double,
            position: Int
        ) {
            binding.imagePlus.setOnClickListener {
                listener.plusOnClick(item, position, newPrice, binding.checkBox.isChecked)
            }
            binding.imageMinus.setOnClickListener {
                listener.minusOnClick(item, position, newPrice, binding.checkBox.isChecked)

            }
            binding.cardParent.setOnClickListener {}
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    totalPrice += (newPrice * item.quantity)
                } else {
                    totalPrice -= (newPrice * item.quantity)
                }
                item.isChecked = isChecked
                listener.cartCheckBox( totalPrice)
            }
        }

    }

}
