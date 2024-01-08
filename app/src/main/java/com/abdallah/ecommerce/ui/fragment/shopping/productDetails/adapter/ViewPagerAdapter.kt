package com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.ProductImagesFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity, private val imagesList: ArrayList<String> ) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return imagesList.size
        }

        override fun createFragment(position: Int): Fragment {
            return ProductImagesFragment(imagesList[position])
        }
}