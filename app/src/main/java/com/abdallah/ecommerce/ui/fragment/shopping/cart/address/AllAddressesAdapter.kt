package com.abdallah.ecommerce.ui.fragment.shopping.cart.address

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.databinding.AllAddressesItemBinding
import com.abdallah.ecommerce.databinding.CartProductItemBinding
import com.abdallah.ecommerce.ui.fragment.shopping.cart.CartRVAdapter

class AllAddressesAdapter(
    val data: ArrayList<AddressModel>,
    val listener: AllAddressesAdapter.AddressOnClick
) :
    RecyclerView.Adapter<AllAddressesAdapter.ViewHolder>() {

    interface AddressOnClick {
        fun onClick(address: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAddressesAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AllAddressesItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AllAddressesAdapter.ViewHolder, position: Int) {
    }
    inner class ViewHolder(val binding: AllAddressesItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}