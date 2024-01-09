package com.abdallah.ecommerce.ui.fragment.shopping.allProducts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.abdallah.ecommerce.utils.animation.RecyclerTouchEffect
import com.bumptech.glide.Glide

class AllCategoriesAdapter(val data: ArrayList<Category>, val itemOnClick : AllCategoryOnClick) :
    RecyclerView.Adapter<AllCategoriesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v: View = inflater.inflate(R.layout.main_category_item, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("SuspiciousIndentation", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = data[position]
        if(category.isSelected) {
            holder.parentArea.setBackgroundResource(R.drawable.blue_background_80)
            holder.txtCategory.setTextColor(MyApplication.myAppContext.resources.getColor(R.color.white))
        }
        else {
            holder.parentArea.setBackgroundResource(R.color.white)
            holder.txtCategory.setTextColor(MyApplication.myAppContext.resources.getColor(R.color.black))

        }
            Glide
                .with(holder.itemView.context)
                .load(category.image)
                .placeholder(CustomShimmerDrawable().shimmerDrawable)
                .error(R.drawable.err_banner)
                .into(holder.imageCategory)
            holder.txtCategory.text = category.categoryName
        holder.parentArea.setOnTouchListener(RecyclerTouchEffect())

        holder.parentArea.setOnClickListener {
            if (category.isSelected)
                return@setOnClickListener

            data.forEach {
                it.isSelected = it == category
            }
            notifyDataSetChanged()

            itemOnClick.allCategoryOnClick(category)

        }

    }
    fun selectSpecificItem(position: Int){
        for(i in 0 until  data.size){
            data[i].isSelected = i == position
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface AllCategoryOnClick{
        fun allCategoryOnClick(category : Category)
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCategory: ImageView
        val txtCategory: TextView
        val parentArea: LinearLayout

        init {
            imageCategory = itemView.findViewById(R.id.imageCategory)
            txtCategory = itemView.findViewById(R.id.txtCategory)
            parentArea = itemView.findViewById(R.id.parentArea)
        }
    }
}
