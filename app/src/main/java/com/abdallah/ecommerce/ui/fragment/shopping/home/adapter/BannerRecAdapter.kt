package com.abdallah.ecommerce.ui.fragment.shopping.home.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class BannerRecAdapter(val data: ArrayList<Uri>? = null, val localImages : ArrayList<Int>? = null ) :
    RecyclerView.Adapter<BannerRecAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v: View = inflater.inflate(R.layout.item_banner_home, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(data!=null)
            Glide
                .with(holder.itemView.context)
                .load(data[position])
                .placeholder(CustomShimmerDrawable().shimmerDrawable)
                .error(R.drawable.err_banner)
                .into(holder.imageBanner)
        else
            Glide
                .with(holder.itemView.context)
                .load(localImages!![position])
                .placeholder(CustomShimmerDrawable().shimmerDrawable)
                .error(R.drawable.err_banner)
                .into(holder.imageBanner)


    }

    override fun getItemCount(): Int {
        return data?.size ?: localImages?.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageBanner: ImageView

        init {
            imageBanner = itemView.findViewById(R.id.imageBanner)
        }
    }
}
