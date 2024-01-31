package com.abdallah.ecommerce.ui.fragment.shopping.cart.address.allAddresses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.databinding.AllAddressesItemBinding

class AllAddressesAdapter(
    val data: ArrayList<AddressModel>,
    val listener: AddressOnClick
) :
    RecyclerView.Adapter<AllAddressesAdapter.ViewHolder>() {
    var lastSelectedItem = 0

    interface AddressOnClick {
        fun onClick(address: AddressModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AllAddressesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    fun selectSpecificItem(position: Int) {
        data[lastSelectedItem].isSelected = false
        lastSelectedItem = position
        data[position].isSelected = true
        notifyDataSetChanged()
    }

    fun removeSelectedItem() {
        data[lastSelectedItem].isSelected = false
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        if (item.isSelected) {
            holder.binding.parentArea.setBackgroundResource(R.drawable.white_background_bordered)


        } else {
            holder.binding.parentArea.setBackgroundResource(R.drawable.white_background)
        }
        holder.binding.address.text = item.addressTitle
        holder.binding.name.text = item.fullName
        holder.binding.phoneNumber.text = item.phone
        holder.binding.parentArea.setOnClickListener {
            if (item.isSelected)
                return@setOnClickListener

            data[position].isSelected = true
            data[lastSelectedItem].isSelected = false
            lastSelectedItem = position
            notifyDataSetChanged()
            listener.onClick(item)
        }
    }

    inner class ViewHolder(val binding: AllAddressesItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}