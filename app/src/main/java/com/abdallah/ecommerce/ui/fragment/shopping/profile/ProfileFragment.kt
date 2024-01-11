package com.abdallah.ecommerce.ui.fragment.shopping.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.sharedPreferences.SharedPreferencesHelper
import com.abdallah.ecommerce.databinding.FragmentProductDetailsBinding
import com.abdallah.ecommerce.databinding.FragmentProfileBinding
import com.abdallah.ecommerce.ui.activity.LoginRegisterActivity
import com.abdallah.ecommerce.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)
        fragOnClick()
        return binding.root
    }

    private fun fragOnClick(){
        binding.logout.setOnClickListener {
            if (firebaseAuth.currentUser !=null){
                firebaseAuth.signOut()
            }
            if (SharedPreferencesHelper.getBoolean(Constant.IS_SKIP )){
                SharedPreferencesHelper.addBoolean(Constant.IS_SKIP , false)
            }
            startActivity(Intent(context , LoginRegisterActivity::class.java))



        }
    }

}