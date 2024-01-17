package com.abdallah.ecommerce.ui.fragment.shopping.home.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.BestDealsItemBinding
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.abdallah.ecommerce.utils.animation.RecyclerTouchEffect
import com.bumptech.glide.Glide

class BestDealsAdapter() :
    RecyclerView.Adapter<BestDealsAdapter.ViewHolder>() {
    lateinit var listener : BestDealsTestOnClick
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BestDealsItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }
    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = differ.currentList[position]

        Glide.with(holder.itemView.context)
                .load(item.productMainImg)
                .placeholder(CustomShimmerDrawable().shimmerDrawable)
                .error(R.drawable.err_banner)
                .into(holder.binding.bestDealsImg)
        val newPrice = item.price!! - item.offerValue!!
        holder.binding.itemOldPrice.paintFlags = holder.binding.itemOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        holder.binding.itemOldPrice.text ="EGP "+item.price.toString()
        holder.binding.offerPercentage.text = "${item.offerPercentage}% Off"
        holder.binding.itemNewPrice.text = "EGP $newPrice"
        holder.binding.itemName.text = item.productName

        holder.binding.parentArea.setOnClickListener {
            listener.itemOnClick(item , holder.binding.bestDealsImg)
        }
        holder.binding.cart.setOnClickListener {
            listener.cartOnClick(item.id , item)
        }
        holder.binding.parentArea.setOnTouchListener(RecyclerTouchEffect())



    }

    interface BestDealsTestOnClick{
        fun itemOnClick(product: Product , view : View)
        fun cartOnClick(productId : String , product: Product)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ViewHolder(itemView: BestDealsItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        var binding : BestDealsItemBinding
        init {
            binding = itemView
        }
    }
}
