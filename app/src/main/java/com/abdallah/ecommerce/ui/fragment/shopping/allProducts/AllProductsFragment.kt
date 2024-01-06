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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllProductsFragment : Fragment(), AllCategoriesAdapter.AllCategoryOnClick {

    lateinit var binding: FragmentAllProductsBinding
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
        initAllCategoriesRv()
        startProductShimmer()
        fragmentOnclick()
        setAppbarTitle()

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
    private fun initAllCategoriesRv() {
        args.categories ?: return
        val categoriesAdapter = AllCategoriesAdapter(args.categories!!, this)
        binding.categoriesRv.adapter = categoriesAdapter
        RecyclerAnimation.animateRecycler(binding.categoriesRv)
        categoriesAdapter.notifyDataSetChanged()
        binding.categoriesRv.scheduleLayoutAnimation()
        binding.categoriesRv.smoothScrollToPosition(args.position)
        categoriesAdapter.selectSpecificItem(args.position)
        getProduct(args.categoryName)

    }

    private fun initAllProductsRv(data: ArrayList<Product>) {
        val allProductsAdapter = AllProductsAdapter(data)
        binding.productRv.adapter = allProductsAdapter
        RecyclerAnimation.animateRecycler(binding.productRv)
        allProductsAdapter.notifyDataSetChanged()
        binding.productRv.scheduleLayoutAnimation()
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
                            initAllProductsRv(result.data)



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
    override fun allCategoryOnClick(category: Category) {
        getProduct(category.categoryName!!)
    }


}