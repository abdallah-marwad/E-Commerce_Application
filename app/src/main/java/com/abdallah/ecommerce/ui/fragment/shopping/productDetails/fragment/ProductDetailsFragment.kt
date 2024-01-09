package com.abdallah.ecommerce.ui.fragment.shopping.productDetails.fragment

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.ColorModel
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.data.model.SizesModel
import com.abdallah.ecommerce.databinding.FragmentProductDetailsBinding
import com.abdallah.ecommerce.ui.activity.ShoppingActivity
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter.ColorsAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter.ReviewsAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter.ViewPagerAdapter
import com.abdallah.ecommerce.utils.BottomSheets.RatingDialog
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.animation.ZoomOutPageTransformer
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.bumptech.glide.Glide


class ProductDetailsFragment : Fragment() {

    private lateinit var binding: FragmentProductDetailsBinding
    private lateinit var product: Product
    private lateinit var ratingDialog: RatingDialog
    private val args: ProductDetailsFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = args.productDetails
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
        val shoppingActivity = activity as ShoppingActivity
        shoppingActivity.hideNavBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bestDealsImg.transitionName = Constant.PRODUCT_TRANSITION_NAME
        fragmentOnclick()
        initViews()
        showProductMainImage()

    }
    private fun fragmentOnclick(){
        binding.toolbar.icBack.setOnClickListener {
            Navigation.findNavController(requireView()).popBackStack()
        }
        binding.btnAddToCart.setOnClickListener {
            if (AppDialog().showingRegisterDialogIfNotRegister(
                    Constant.COULDNOT_ADD_TO_CART,
                    Constant.PLS_LOGIN
                ).not()
            ) {
                return@setOnClickListener
            }
        }
        binding.addReview.setOnClickListener {
            ratingDialog = RatingDialog()
            ratingDialog.showDialog { s, fl ->

            }
        }
    }
    private fun initViews(){
        val newPrice = product.price!! - product.offerValue!!

        binding.toolbar.title.text = "Product Details"
        binding.tvProductName.text = product.productName
        binding.tvProductDescription.text = product.productdescription
        binding.tvProductPrice.text = "EGP "+ newPrice
        binding.category.text = "Category : "+ product.categoryName
        binding.ratingNumber.text = "(${product.ratersNum})"
        binding.itemRatingBar.rating = if( product.rating <=0) 5F else  product.rating
        handleProductOfferPrice()
        setupViewPager()
        initColorsRv()
        initSizesRv()
        initReviewsRv()
    }
    private fun initColorsRv(){
        val colorsList = product.productColors
        val colorModelList  = ArrayList<ColorModel>()
        colorsList?.forEach {
            colorModelList.add(ColorModel(it , false))
        }
        val colorsAdapter = ColorsAdapter(colorModelList , null)
        binding.rvColors.adapter = colorsAdapter
        RecyclerAnimation.animateRecycler(binding.rvColors)
        colorsAdapter.notifyDataSetChanged()
        binding.rvColors.scheduleLayoutAnimation()
    }
    private fun initReviewsRv(){
        val reviewsList = product.ratingList
        if(reviewsList.isEmpty()){
            binding.reviewArae.visibility = View.GONE
            return
        }
        val reviewsAdapter = ReviewsAdapter(reviewsList)
        binding.reviewsRV.adapter = reviewsAdapter
        RecyclerAnimation.animateRecycler(binding.reviewsRV)
        reviewsAdapter.notifyDataSetChanged()
        binding.reviewsRV.scheduleLayoutAnimation()

    }
    private fun initSizesRv(){
        val sizesList = product.productSize
        val sizesModelList  = ArrayList<SizesModel>()
        sizesList?.forEach {
            sizesModelList.add(SizesModel(it , false))
        }
        val sizesAdapter = ColorsAdapter(null , sizesModelList)
        binding.rvSizes.adapter = sizesAdapter
        RecyclerAnimation.animateRecycler(binding.rvSizes)
        sizesAdapter.notifyDataSetChanged()
        binding.rvSizes.scheduleLayoutAnimation()

    }
    private fun setupViewPager(){
        val adapter = ViewPagerAdapter(requireActivity() , product.productImages!!)
        binding.viewpager.adapter = adapter
        binding.viewpager.setPageTransformer(ZoomOutPageTransformer())
        binding.dotsIndicator.setViewPager2(binding.viewpager)

    }
    private fun handleProductOfferPrice(){
        if(product.offerValue!=null && product.offerValue!! > 0){
            binding.tvProductOfferPrice.text = product.price.toString()
            binding.tvProductOfferPrice.paintFlags = binding.tvProductOfferPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG


        }
    }
    private fun showProductMainImage(){
        Glide.with(requireContext())
            .load(args.productDetails.productMainImg)
            .error(R.drawable.err_banner)
            .placeholder(CustomShimmerDrawable().shimmerDrawable)
            .into(binding.bestDealsImg)

    }

}