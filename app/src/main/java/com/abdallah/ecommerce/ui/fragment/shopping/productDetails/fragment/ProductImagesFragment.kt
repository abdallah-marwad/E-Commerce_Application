package com.abdallah.ecommerce.ui.fragment.shopping.productDetails.fragment

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.databinding.FragmentProductImagesBinding
import com.abdallah.ecommerce.databinding.FragmentShoppingHomeBinding
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.bumptech.glide.Glide

class ProductImagesFragment() : Fragment() {

    lateinit var imgUri : String
    private lateinit var binding: FragmentProductImagesBinding

    constructor(imgUri: String) : this() {
        this.imgUri = imgUri
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductImagesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext())
            .load(imgUri)
            .placeholder(CustomShimmerDrawable().shimmerDrawable)
            .error(R.drawable.err_banner)
            .into(binding.productImages)

    }

}