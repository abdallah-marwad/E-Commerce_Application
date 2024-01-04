package com.abdallah.ecommerce.ui.fragment.shopping.allProducts

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.FragmentAllProductsBinding
import com.abdallah.ecommerce.ui.fragment.shopping.allProducts.adapter.AllCategoriesAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.allProducts.adapter.AllProductsAdapter
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.ViewAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllProductsFragment : Fragment(), AllCategoriesAdapter.AllCategoryOnClick {

    lateinit var binding: FragmentAllProductsBinding
    private val allProductsAdapter  :  AllProductsAdapter by lazy { AllProductsAdapter(ArrayList())}
    private val args: AllProductsFragmentArgs by navArgs()
    private val viewModel by viewModels<AllProductsViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllProductsBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.categoryName = args.categoryName
        initAllCategoriesRv()
        startProductShimmer()
        fragmentOnclick()
        setAppbarTitle()
        setProgressBarAccordingToLoadState()
        setProductsAdapter()
        getProducts()

    }

    private fun fragmentOnclick(){
        binding.toolbar.icBack.setOnClickListener {
            Navigation.findNavController(requireView()).popBackStack()
        }
    }
    private fun setAppbarTitle(){
        binding.toolbar.title.text = "All Products"
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getProduct(categoryName: String) {
        viewModel.getProductsByCategory(categoryName)
        lifecycleScope.launchWhenResumed {
                viewModel.productsFlow.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            stopProductShimmer()
                            if (result.data!!.isEmpty()) {
                                ViewAnimation().viewAnimation(binding.noProducts ,binding.parentArea)
                                binding.noProducts.visibility = View.VISIBLE
                            }
//                            initAllProductsRv(result.data)



                        }

                        is Resource.Failure -> {
                            stopProductShimmer()
                            Toast.makeText(
                                context, "Error occured " + result.message,
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d("test", "Firebase Err ${result.message}")
                        }

                        is Resource.Loading -> {
                            startProductShimmer()
                        }

                        else -> {}
                    }

                }
            }
        }



    private fun stopProductShimmer() {
        binding.shimmerProducts.stopShimmer()
        binding.shimmerProducts.visibility = View.INVISIBLE
        binding.productRv.visibility = View.VISIBLE

    }

    private fun startProductShimmer() {
        binding.productRv.visibility = View.INVISIBLE
        binding.shimmerProducts.startShimmer()
        binding.shimmerProducts.visibility = View.VISIBLE
        binding.noProducts.visibility = View.GONE

    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initAllCategoriesRv() {
        val list = args.categories ?: return

        val categoriesAdapter = AllCategoriesAdapter(list, this)
        binding.categoriesRv.adapter = categoriesAdapter
        RecyclerAnimation.animateRecycler(binding.categoriesRv)
        categoriesAdapter.notifyDataSetChanged()
        binding.categoriesRv.scheduleLayoutAnimation()
        binding.categoriesRv.smoothScrollToPosition(args.position)
        categoriesAdapter.selectSpecificItem(args.position)
//        getProduct(args.categoryName)

    }

//    private fun initAllProductsRv(data: ArrayList<Product>) {
//         allProductsAdapter = AllProductsAdapter(data)
//        binding.productRv.adapter = allProductsAdapter
//        RecyclerAnimation.animateRecycler(binding.productRv)
//        allProductsAdapter.notifyDataSetChanged()
//        binding.productRv.scheduleLayoutAnimation()
//    }
    private fun setProgressBarAccordingToLoadState() {
     lifecycleScope.launch {
            allProductsAdapter.loadStateFlow.collectLatest {
                binding.paggingProgress.isVisible = it.append is LoadState.Loading
            }
        }
    }
    private fun setProductsAdapter() {
        binding.productRv.adapter = allProductsAdapter
    }

    private fun getProducts() {
        lifecycleScope.launch {
            viewModel.flow.collectLatest {product->
                stopProductShimmer()
                allProductsAdapter.submitData(product)
                allProductsAdapter.notifyDataSetChanged()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun allCategoryOnClick(category: Category) {
//        getProduct(category.categoryName!!)
        viewModel.categoryName = category.categoryName!!
        getProducts()
    }


}