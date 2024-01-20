package com.abdallah.ecommerce.ui.activity.shopping

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.databinding.ActivityShoppingBinding
import com.abdallah.ecommerce.utils.LangHelper
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity() {
    private lateinit var binding :ActivityShoppingBinding
    private val viewModel by viewModels<ShoppingActivityViewModel>()
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d("test" , "screen : ShoppingActivity")
        LangHelper.makeLangEn()
        binding = ActivityShoppingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpNavController()
        getCartCount()
        cartCountCallback()
    }
    private fun setUpNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        binding.bottomNavigation.setupWithNavController(navHostFragment.navController)
    }
     fun hideNavBar() {
         binding.bottomNavigation.visibility = View.GONE

     }
    fun showNavBar() {
       binding.bottomNavigation.visibility = View.VISIBLE
    }
    private fun cartCountCallback() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartSize.collect {
                    when (it) {
                        is Resource.Success -> {
                            binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                                backgroundColor = resources.getColor(R.color.secondary_color)
                                number = it.data ?: 0
                            }
                        }

                        is Resource.Failure -> {
                            binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                                backgroundColor = resources.getColor(R.color.secondary_color)
                            }
                        }

                        is Resource.Loading -> {
                            binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                                backgroundColor = resources.getColor(R.color.secondary_color)
                            }
                        }

                        else -> {}
                    }
                }
            }}

    }
    private fun getCartCount() {
        if(firebaseAuth.currentUser == null ){
            return
        }
        if(firebaseAuth.currentUser!!.uid =="" ){
            return
        }
        viewModel.getItemsInCart(
            firebaseAuth.currentUser!!.uid
        )
    }
}