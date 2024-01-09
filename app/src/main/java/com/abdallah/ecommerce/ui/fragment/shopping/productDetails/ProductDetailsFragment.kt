package com.abdallah.ecommerce.ui.fragment.shopping.productDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.abdallah.ecommerce.databinding.FragmentProductDetailsBinding
import com.abdallah.ecommerce.utils.Constant

class ProductDetailsFragment : Fragment() {

    lateinit var binding: FragmentProductDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        sharedElementReturnTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductDetailsBinding.inflate(inflater)
        binding.bestDealsImg.transitionName = Constant.BEST_DEALS_TRANSACTION_NAME

        return binding.root
    }

}