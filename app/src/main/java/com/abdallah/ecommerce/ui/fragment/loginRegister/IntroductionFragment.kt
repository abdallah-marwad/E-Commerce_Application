package com.abdallah.ecommerce.ui.fragment.loginRegister

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.sharedPreferences.SharedPreferencesHelper
import com.abdallah.ecommerce.databinding.FragmentIntroductionBinding
import com.abdallah.ecommerce.utils.Constant
import org.greenrobot.eventbus.EventBus


class IntroductionFragment : Fragment() {
    private lateinit var binding: FragmentIntroductionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntroductionBinding.inflate(inflater)
        if(SharedPreferencesHelper.getBoolean(Constant.NOT_FIRST_TIME)) {
            findNavController().navigate(R.id.action_introductionFragment_to_accountOptionFragment)
            return null
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("screen", "IntroductionFragment ")

        binding.btnStart.setOnClickListener{
            findNavController().navigate(R.id.action_introductionFragment_to_accountOptionFragment)
        }
    }


}