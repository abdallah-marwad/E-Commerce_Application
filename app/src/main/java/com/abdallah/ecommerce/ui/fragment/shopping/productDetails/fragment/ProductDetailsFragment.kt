package com.abdallah.ecommerce.ui.fragment.shopping.productDetails.fragment

import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.ColorModel
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.data.model.RatingModel
import com.abdallah.ecommerce.data.model.SizesModel
import com.abdallah.ecommerce.databinding.FragmentProductDetailsBinding
import com.abdallah.ecommerce.ui.activity.shopping.ShoppingActivity
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter.ColorsAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter.ReviewsAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.adapter.ViewPagerAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.productDetails.viewmodel.ProductDetailsViewModel
import com.abdallah.ecommerce.utils.BottomSheets.RatingDialog
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.CustomShimmerDrawable
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.animation.ZoomOutPageTransformer
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ProductDetailsFragment : Fragment(), ColorsAdapter.SelectedColorAndSize {

    private lateinit var binding: FragmentProductDetailsBinding
    private lateinit var product: Product

    private val ratingDialog by lazy { RatingDialog() }
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var updateWhenBack = false
    private var selectedColor = -2
    private var selectedSize: String? = null
    private lateinit var colorsAdapter: ColorsAdapter

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private val args: ProductDetailsFragmentArgs by navArgs()
    private val viewModel by viewModels<ProductDetailsViewModel>()


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
        addReviewCallback()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bestDealsImg.transitionName = Constant.PRODUCT_TRANSITION_NAME
        fragmentOnclick()
        initViews()
        addProductToCartCallback()
        showProductMainImage()
        noInternetCallback()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun fragmentOnclick() {
        binding.toolbar.icBack.setOnClickListener {
            if (updateWhenBack) {
                findNavController().navigate(R.id.action_productDetailsFragment_to_homeFragment)
                return@setOnClickListener
            }
            Navigation.findNavController(requireView()).popBackStack()
        }
        binding.addReview.setOnClickListener {
            val list =
                product.ratingList.filter { (it.raterId == firebaseAuth.currentUser?.email) }
            if (list.isNotEmpty()) {
                ratingDialog.showDialog(
                    canEditReview = false,
                    comment = list[0].comment,
                    ratingParm = list[0].rating
                )
                return@setOnClickListener
            }
            ratingDialog.showDialog { comment, rating ->
                addReview(comment, rating)
            }
        }
        binding.btnAddToCart.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                AppDialog().showingRegisterDialogIfNotRegister(
                    Constant.COULDNOT_DO_THIS_ACTON,
                    Constant.PLS_LOGIN
                )
                return@setOnClickListener
            }
            if (validateColorAndSizeSelections())
                addProductToCart()
        }
    }

    private fun validateColorAndSizeSelections(): Boolean {
        var isSelected = true
        if (selectedColor == -2) {
            binding.tvColorError.visibility = View.VISIBLE
            Snackbar.make(binding.nestedParent, "Select color", Toast.LENGTH_SHORT)
                .show()
            isSelected = false
        }
        if (selectedSize == null) {
            binding.tvSizeError.visibility = View.VISIBLE
            Snackbar.make(binding.nestedParent, "Select size", Toast.LENGTH_SHORT)
                .show()
            isSelected = false
        }
        return isSelected
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addProductToCart() {
        viewModel.addProductToCart(
            firebaseAuth.currentUser?.uid ?: "",
            product,
            selectedColor,
            selectedSize!!
        )
    }

    private fun addProductToCartCallback() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addToCartFlow.collect {
                    when (it) {
                        is Resource.Success -> {
                            binding.btnAddToCart.revertAnimation()
                            Toast.makeText(context, "Product added successfully", Toast.LENGTH_LONG)
                                .show()
                        }

                        is Resource.Loading -> {
                            binding.btnAddToCart.startAnimation()
                        }

                        is Resource.Failure -> {
                            binding.btnAddToCart.revertAnimation()
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                            Log.d("test", "addProductToCartCallback " + it.message)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun noInternetCallback() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noInternet.collect {
                    Snackbar.make(
                        binding.nestedParent,
                        "No Internet connection",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addReview(comment: String, rating: Float) {
        if (firebaseAuth.currentUser == null) {
            AppDialog().showingRegisterDialogIfNotRegister(
                Constant.COULDNOT_DO_THIS_ACTON,
                Constant.PLS_LOGIN
            )
            return
        }
        viewModel.addReview(
            product.ratersNum!!,
            product.rating,
            rating.toInt(),
            product.id,
            comment,
            product.ratingList,
            firebaseAuth.currentUser?.email ?: "",
            firebaseAuth.currentUser?.displayName ?: "Anonymous",
        )
    }

    private fun addReviewCallback() {
        lifecycleScope.launchWhenResumed {
            viewModel.review.collect {
                when (it) {
                    is Resource.Success -> {
                        updateWhenBack = true
                        changeDataStaticlly(it.data!!)
                        ratingDialog.hideLoader()
                        ratingDialog.dismiss()

                    }

                    is Resource.Loading -> {
                        ratingDialog.showLoader()
                    }

                    is Resource.Failure -> {
                        ratingDialog.hideLoader()
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun changeDataStaticlly(hashMap: HashMap<String, Any>) {
        try {
            product.rating = hashMap["rating"] as Float
            product.ratersNum = hashMap["ratersNum"] as Int
            binding.reviewArae.visibility = View.VISIBLE
            binding.ratingNumber.text = hashMap["ratersNum"].toString()
            binding.itemRatingBar.rating = hashMap["rating"] as Float
            reviewsAdapter.list = hashMap["ratingList"] as ArrayList<RatingModel>
            reviewsAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("test", "Exception is in side changeDataStaticlly" + e.message)
        }


    }

    private fun initViews() {
        val newPrice = product.price!! - product.offerValue!!

        binding.toolbar.title.text = "Product Details"
        binding.tvProductName.text = product.productName
        binding.tvProductDescription.text = product.productdescription
        binding.tvProductPrice.text = "EGP " + newPrice
        binding.category.text = "Category : " + product.categoryName
        binding.ratingNumber.text = "(${product.ratersNum})"
        binding.itemRatingBar.rating = if (product.rating <= 0) 5F else product.rating
        handleProductOfferPrice()
        setupViewPager()
        initColorsRv()
        initSizesRv()
        initReviewsRv()
    }

    private fun initColorsRv() {
        val colorsList = product.productColors
        val colorModelList = ArrayList<ColorModel>()
        colorsList?.forEach {
            colorModelList.add(ColorModel(it, false))
        }
        colorsAdapter = ColorsAdapter(colorModelList, null, this)
        binding.rvColors.adapter = colorsAdapter
        RecyclerAnimation.animateRecycler(binding.rvColors)
        colorsAdapter.notifyDataSetChanged()
        binding.rvColors.scheduleLayoutAnimation()
    }

    private fun initReviewsRv() {
        val reviewsList = product.ratingList
        if (reviewsList.isEmpty()) {
            binding.reviewArae.visibility = View.GONE
        }
        reviewsAdapter = ReviewsAdapter(reviewsList)
        binding.reviewsRV.adapter = reviewsAdapter
        RecyclerAnimation.animateRecycler(binding.reviewsRV)
        reviewsAdapter.notifyDataSetChanged()
        binding.reviewsRV.scheduleLayoutAnimation()

    }

    private fun initSizesRv() {
        val sizesList = product.productSize
        val sizesModelList = ArrayList<SizesModel>()
        sizesList?.forEach {
            sizesModelList.add(SizesModel(it, false))
        }
        if (sizesModelList.isEmpty())
            selectedSize = ""
        val sizesAdapter = ColorsAdapter(null, sizesModelList, this)
        binding.rvSizes.adapter = sizesAdapter
        RecyclerAnimation.animateRecycler(binding.rvSizes)
        sizesAdapter.notifyDataSetChanged()
        binding.rvSizes.scheduleLayoutAnimation()

    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(requireActivity(), product.productImages!!)
        binding.viewpager.adapter = adapter
        binding.viewpager.setPageTransformer(ZoomOutPageTransformer())
        binding.dotsIndicator.setViewPager2(binding.viewpager)

    }

    private fun handleProductOfferPrice() {
        if (product.offerValue != null && product.offerValue!! > 0) {
            binding.tvProductOfferPrice.text = product.price.toString()
            binding.tvProductOfferPrice.paintFlags =
                binding.tvProductOfferPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    private fun showProductMainImage() {
        Glide.with(requireContext())
            .load(args.productDetails.productMainImg)
            .error(R.drawable.err_banner)
            .placeholder(CustomShimmerDrawable().shimmerDrawable)
            .into(binding.bestDealsImg)

    }

    override fun selectedColor(color: Int) {
        selectedColor = color
        binding.tvColorError.visibility = View.GONE

    }

    override fun selectedSize(size: String) {
        selectedSize = size
        binding.tvSizeError.visibility = View.GONE

    }

}