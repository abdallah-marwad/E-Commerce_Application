package com.abdallah.ecommerce.ui.fragment.shopping.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.databinding.SearchHistoryItemBinding

class RecentSearchAdapter(val data: ArrayList<String>, val listener: HistoryOnClick) :
    RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SearchHistoryItemBinding.inflate(inflater)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.binding.historyTxt.text = item
        holder.binding.copyTxt.setOnClickListener {
            listener.copyOnClick(item)
        }
        holder.binding.parnetArea.setOnClickListener {
            listener.itemOnClick(item)
        }

    }

    interface HistoryOnClick {
        fun itemOnClick(searchTxt: String)
        fun copyOnClick(searchTxt: String)
    }

    inner class ViewHolder(val binding: SearchHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}