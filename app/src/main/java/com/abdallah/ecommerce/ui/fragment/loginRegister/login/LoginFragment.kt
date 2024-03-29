package com.abdallah.ecommerce.ui.fragment.loginRegister.login

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.sharedPreferences.SharedPreferencesHelper
import com.abdallah.ecommerce.databinding.FragmentLoginBinding
import com.abdallah.ecommerce.ui.activity.shopping.ShoppingActivity
import com.abdallah.ecommerce.utils.BottomSheets.showResetPasswordDialog
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.SmsBroadcastReceiver
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint

class LoginFragment : Fragment(R.layout.fragment_login), View.OnClickListener {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("screen", "screen is LoginFragment ")
        binding = FragmentLoginBinding.inflate(inflater)
        SharedPreferencesHelper.addBoolean(Constant.NOT_FIRST_TIME, true)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLoginLoginFrag.setOnClickListener(this)
        binding.forgetPassword.setOnClickListener(this)
        noInternetCallBack()
        loginCallBack()
        validationCallBack()
        resetPasswordCallBack()
        fragOnClick()
    }
    private fun fragOnClick() {
        binding.skip.setOnClickListener {
            SharedPreferencesHelper.addBoolean(Constant.IS_SKIP, true)
            startActivity(Intent(context, ShoppingActivity::class.java))
            requireActivity().finish()
        }
        binding.register.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun loginCallBack() {
        lifecycleScope.launchWhenStarted {
            viewModel.loginResult.collect { loginResult ->
                when (loginResult) {
                    is Resource.Loading -> {
                        binding.btnLoginLoginFrag.startAnimation()
                    }
                    is Resource.Success -> {
                        binding.btnLoginLoginFrag.revertAnimation()
                        makeText(requireContext(), "successful login", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(context, ShoppingActivity::class.java))
                        activity?.finish()
                    }
                    is Resource.Failure -> {
                        binding.btnLoginLoginFrag.revertAnimation()
                        makeText(requireContext(), loginResult.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    else -> {}
                }
            }
        }
    }


    private fun validationCallBack() {
        lifecycleScope.launchWhenStarted {
            viewModel.failedValidation.collect {
                if (it.email is ValidationState.Invalid) {
                    binding.edEmailLogin.error = it.email.msg
                }
                if (it.password is ValidationState.Invalid) {
                    binding.edPasswordLogin.error = it.password.msg
                }
            }
        }

    }

    private fun noInternetCallBack() {
        lifecycleScope.launchWhenStarted {
            viewModel.noInternet.collect {
                Snackbar.make(binding.imageView3, "No Internet connection", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun resetPasswordCallBack() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetPassword.collect { state ->
                    when (state) {
                        is Resource.Success -> {
                            state.data?.let {
                                Snackbar.make(binding.forgetPassword, it, Snackbar.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        is Resource.UnSpecified -> {}
                        is Resource.Loading -> {}
                        is Resource.Failure -> {
                            state.message?.let {
                                Snackbar.make(binding.forgetPassword, it, Snackbar.LENGTH_SHORT)
                                    .show()
                            }
                        }

                    }
                }
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(view: View?) {
        val id = view?.id
        if (id == binding.btnLoginLoginFrag.id) {
            val email = binding.edEmailLogin.text.toString().trim()
            val password = binding.edPasswordLogin.text.toString()
            viewModel.login(email, password)
        }


        if (id == binding.forgetPassword.id) {
            showResetPasswordDialog { email, validEmail, errMsg ->
                if (validEmail)
                    viewModel.resetPassword(email)
                else {
                    makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}