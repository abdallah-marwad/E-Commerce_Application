package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.databinding.CartProductItemBinding
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.bumptech.glide.Glide

class CartRVAdapterTest(var list: MutableList<CartProduct>, val listener: CartOnClickTest) :
    RecyclerView.Adapter<CartRVAdapterTest.ViewHolder>() {

    var totalPrice = 0.0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CartProductItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = list[holder.layoutPosition]
        val newPrice = item.product.price!! - item.product.offerValue!!
        holder.initViews(item, newPrice)
        holder.handleClicks(item, newPrice, holder.layoutPosition)

    }

    interface CartOnClickTest {
        fun itemOnClickTest(product: CartProduct, view: View)
        fun plusOnClickTest(product: CartProduct, position: Int, price: Double, isChecked: Boolean)
        fun minusOnClickTest(product: CartProduct, position: Int, price: Double, isChecked: Boolean)
        fun cartCheckBoxTest(totalPrice: Double)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun initViews(item: CartProduct, newPrice: Double) {
            Glide.with(itemView.context)
                .load(item.product.productMainImg)
                .placeholder(CustomShimmerDrawable().shimmerDrawable)
                .error(R.drawable.err_banner)
                .into(binding.imageCartProduct)
            binding.tvProductCartName.text = item.product.productName
            binding.imageCartProductColor.setImageDrawable(
                ColorDrawable(item.color)
            )
            if (item.size.isEmpty() || item.size == "")
                binding.tvCartProductSize.visibility = View.GONE
            else {
                binding.tvCartProductSize.text = item.size
                binding.tvCartProductSize.visibility = View.VISIBLE

            }
            binding.tvCartProductQuantity.text = item.quantity.toString()
            binding.tvProductCartPrice.text = newPrice.toString()
        }

        fun handleClicks(
            item: CartProduct,
            newPrice: Double,
            position: Int,
        ) {
            binding.imagePlus.setOnClickListener {
                listener.plusOnClickTest(item, position, newPrice, binding.checkBox.isChecked)
            }
            binding.imageMinus.setOnClickListener {
                listener.minusOnClickTest(item, position, newPrice, binding.checkBox.isChecked)
            }
            binding.cardParent.setOnClickListener {}
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    totalPrice += (newPrice * item.quantity)
                } else {
                    totalPrice -= (newPrice * item.quantity)
                }
                item.isChecked = isChecked
                listener.cartCheckBoxTest(totalPrice)
            }
        }

    }

    fun setAdapterList(updatedList: List<CartProduct>) {
        val diffResult = DiffUtil.calculateDiff(CartCallback(list, updatedList))
        list.clear()
        list.addAll(updatedList)
        diffResult.dispatchUpdatesTo(this)
    }

    class CartCallback(
        private val oldList: List<CartProduct>,
        private val newList: List<CartProduct>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].product.id === newList[newItemPosition].product.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            Log.d(
                "test",
                "areContentsTheSame = " + (oldList[oldItemPosition] == newList[newItemPosition])
            )
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

}
