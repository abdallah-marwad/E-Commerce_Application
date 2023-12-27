package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.bumptech.glide.Glide

class MainCategoryAdapter(val data: ArrayList<Category> ) :
    RecyclerView.Adapter<MainCategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v: View = inflater.inflate(R.layout.main_category_item, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = data[position]
            Glide
                .with(holder.itemView.context)
                .load(category.image)
                .placeholder(CustomShimmerDrawable().shimmerDrawable)
                .error(R.drawable.err_banner)
                .into(holder.imageCategory)
            holder.txtCategory.text = category.categoryName


    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCategory: ImageView
        val txtCategory: TextView

        init {
            imageCategory = itemView.findViewById(R.id.imageCategory)
            txtCategory = itemView.findViewById(R.id.txtCategory)
        }
    }
}
