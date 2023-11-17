package com.abdallah.ecommerce.ui.fragment.login

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.databinding.FragmentLoginBinding
import com.abdallah.ecommerce.ui.activity.ShoppingActivity
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.dialogs.showResetPasswordDialog
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class LoginFragment : Fragment(R.layout.fragment_login), View.OnClickListener {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<LoginViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("screen", "LoginFragment ")

        binding = FragmentLoginBinding.inflate(inflater)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLoginLoginFrag.setOnClickListener(this)
        binding.forgetPassword.setOnClickListener(this)
        noInternetCallBack()
        loginState()
        validationState()
        resetPasswordCallBack()
    }

    private fun loginState() {
        lifecycleScope.launchWhenStarted {

            viewModel.loginResult.collect { loginResult ->
                when (loginResult) {
                    is Resource.Loading -> {
                        binding.btnLoginLoginFrag.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.btnLoginLoginFrag.revertAnimation()
                        startActivity(Intent(requireContext(), ShoppingActivity::class.java))
                    }

                    is Resource.Failure -> {
                        binding.btnLoginLoginFrag.revertAnimation()
                        Toast.makeText(requireContext(), loginResult.message, Toast.LENGTH_LONG)

                    }

                    else -> {}
                }
            }
        }
    }

    private fun validationState() {
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
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG)
                    .show()

            }
        }

    }

    private fun resetPasswordCallBack() {
        lifecycleScope.launchWhenStarted {
            viewModel.resetPassword.collect { state ->
                when (state) {
                    is Resource.Success -> {
                        state.data?.let {
                            Snackbar.make(binding.forgetPassword, it, Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    is Resource.UnSpecified -> {}
                    is Resource.Loading -> {}
                    is Resource.Failure -> {
                        state.message?.let {
                            Snackbar.make(binding.forgetPassword, it, Toast.LENGTH_LONG)
                                .show()
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