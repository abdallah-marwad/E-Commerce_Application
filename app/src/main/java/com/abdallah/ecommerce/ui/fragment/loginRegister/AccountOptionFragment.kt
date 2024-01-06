package com.abdallah.ecommerce.ui.fragment.loginRegister

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.sharedPreferences.SharedPreferencesHelper
import com.abdallah.ecommerce.databinding.FragmentAccountOptionsBinding
import com.abdallah.ecommerce.utils.Constant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AccountOptionFragment : Fragment() {
    private lateinit var binding: FragmentAccountOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountOptionsBinding.inflate(inflater)
        if(SharedPreferencesHelper.getBoolean(Constant.NOT_FIRST_TIME)) {
            findNavController().navigate(R.id.action_accountOptionFragment_to_loginFragment)
            return null
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("screen", "AccountOptionFragment ")

        binding.btnRegister.setOnClickListener{
            findNavController().navigate(R.id.action_accountOptionFragment_to_registerFragment)
        }
        binding.btnSignIn.setOnClickListener{
            findNavController().navigate(R.id.action_accountOptionFragment_to_loginFragment)
        }
    }
}