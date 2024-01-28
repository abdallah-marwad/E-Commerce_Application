package com.abdallah.ecommerce.ui.fragment.shopping.cart.address.allAddresses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.databinding.AllAddressesItemBinding

class AllAddressesAdapter(
    val data: ArrayList<AddressModel>,
    val listener: AddressOnClick
) :
    RecyclerView.Adapter<AllAddressesAdapter.ViewHolder>() {

    interface AddressOnClick {
        fun onClick(address: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AllAddressesItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }
    inner class ViewHolder(val binding: AllAddressesItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}