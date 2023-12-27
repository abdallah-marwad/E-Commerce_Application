package com.abdallah.ecommerce.ui.fragment.shopping.home

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
import com.google.firebase.storage.StorageReference

class BannerRecAdapter(val data: ArrayList<Uri> ) :
    RecyclerView.Adapter<BannerRecAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v: View = inflater.inflate(R.layout.item_banner_home, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder,  position: Int) {
            Glide
                .with(holder.itemView.context)
                .load(data[position])
                .listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("test", e?.message +"")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("test", "position from rec is $position with data ${data[position]}")
                        return false
                    }
                })
                .placeholder(R.drawable.err_banner)//CustomShimmerDrawable().shimmerDrawable
                .error(R.drawable.err_banner)
                .into(holder.imageBanner)

        Log.d("test", "position from rec is $position with data ${data[position]}")


    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageBanner: ImageView

        init {
            imageBanner = itemView.findViewById(R.id.imageBanner)
        }
    }
}
