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
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.FragmentAllProductsBinding
import com.abdallah.ecommerce.ui.activity.ShoppingActivity
import com.abdallah.ecommerce.ui.fragment.shopping.allProducts.adapter.AllCategoriesAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.allProducts.adapter.AllProductsAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.home.ShoppingHomeFragmentDirections
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.ViewAnimation
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AllProductsFragment : Fragment(), AllCategoriesAdapter.AllCategoryOnClick , AllProductsAdapter.AllProductsOnClick{

    lateinit var binding: FragmentAllProductsBinding
    var categoriesAdapter : AllCategoriesAdapter? = null
    private val appDialog by lazy { AppDialog() }
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private val args: AllProductsFragmentArgs by navArgs()
    private val viewModel by viewModels<AllProductsViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllProductsBinding.inflate(inflater)
        val shoppingActivity = activity as ShoppingActivity
        shoppingActivity.hideNavBar()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAllCategoriesRv()
        startProductShimmer()
        fragmentOnclick()
        setAppbarTitle()
        addProductToCartCallback()

    }

    private fun fragmentOnclick(){
        binding.toolbar.icBack.setOnClickListener {
            Navigation.findNavController(requireView()).popBackStack()
            categoriesAdapter?.removeSelectedItem()

        }
    }
    private fun setAppbarTitle(){
        binding.toolbar.title.text = "All Products"
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initAllCategoriesRv() {
        args.categories ?: return
         categoriesAdapter = AllCategoriesAdapter(args.categories!!, this)
        binding.categoriesRv.adapter = categoriesAdapter
        RecyclerAnimation.animateRecycler(binding.categoriesRv)
        categoriesAdapter?.notifyDataSetChanged()
        binding.categoriesRv.scheduleLayoutAnimation()
        binding.categoriesRv.smoothScrollToPosition(args.position)
        categoriesAdapter?.selectSpecificItem(args.position)
        getProduct(args.categoryName)

    }

    private fun initAllProductsRv(data: ArrayList<Product>) {
        val allProductsAdapter = AllProductsAdapter(data , this)
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

    override fun itemOnClick(product: Product, view: View) {
        val extras = FragmentNavigatorExtras(
            view to Constant.PRODUCT_TRANSITION_NAME
        )
        view.transitionName = Constant.PRODUCT_TRANSITION_NAME
        val action =
            AllProductsFragmentDirections.actionAllProductsToProductDetailsFragment(product)
        findNavController().navigate(action, extras)
    }

    override fun cartOnClick(productId: String , product: Product) {
        if(firebaseAuth.currentUser == null){
            AppDialog().showingRegisterDialogIfNotRegister(
                Constant.COULDNOT_ADD_TO_CART,
                Constant.PLS_LOGIN
            )
            return
        }
        addProductToCart(product)
    }
    private fun addProductToCart(product : Product) {
        viewModel.addProductToCart(
            firebaseAuth.currentUser?.email ?: "",
            product,
            -1 ,
            ""
        )
    }

    private fun addProductToCartCallback() {
        lifecycleScope.launchWhenResumed {
            viewModel.addToCartFlow.collect{
                when (it) {
                    is Resource.Success -> {
                        appDialog.dismissProgress()
                        Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Loading -> {
                        appDialog.showProgressDialog()
                    }

                    is Resource.Failure -> {
                        appDialog.dismissProgress()
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        Log.d("test" , "addProductToCartCallback "+it.message)
                    }

                    else -> {}
                }
            }
        }

    }



}