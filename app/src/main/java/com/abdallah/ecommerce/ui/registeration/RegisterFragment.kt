package com.abdallah.ecommerce.ui.registeration

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.User
import com.abdallah.ecommerce.data.registeration.RegisterWithPhone
import com.abdallah.ecommerce.databinding.FragmentRegisterBinding
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val TAG = "RegisterFragment"
    private lateinit var arl: ActivityResultLauncher<Intent>

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("screen", "RegisterFragment ")

        binding = FragmentRegisterBinding.inflate(inflater)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arlInitial()
        registerStateCallBack()
        noInternetCallBack()
        fragOnClicks()
        validationState()


    }



    private fun arlInitial() {
        arl = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
//            try {
            lifecycleScope.launchWhenStarted {
                withContext(Dispatchers.Main) {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result

                    account?.let { googleAccount ->
                        viewModel.googleAuthWithFireBase(googleAccount)
                    }


//            } catch (e: Exception) {
//                Log.d("tests" , "Exception from google :  "+e.localizedMessage)
//            }

                }
            }
        }

    }

    private fun googleSignInRequest() {
        val signInClient = viewModel.googleSignInRequest(requireActivity())
        signInClient.signInIntent.apply {
            arl.launch(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun fragOnClicks() {
        binding.apply {
            btnRegister.setOnClickListener {
                val userName = edEmail.text.toString().trim()
                val password = edpassword.text.toString()
                val firstName = edFirstName.text.toString().trim()
                val lastName = edLastName.text.toString().trim()
                val user = User(
                    firstName,
                    lastName,
                    userName,
                    ""
                )
                viewModel.createAccountWithEmailAndPassword(
                    user,
                    password,
                    requireActivity().application
                )
            }


            phoneLogin.setOnClickListener {
                val registerWithPhone = RegisterWithPhone()
                registerWithPhone.activity = requireActivity()
                registerWithPhone.sendOtp("01551123149")
            }


            binding.gmailLogin.setOnClickListener {
                googleSignInRequest()
            }


        }
    }

    private fun registerStateCallBack() {
        lifecycleScope.launchWhenStarted {
            viewModel.register.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.btnRegister.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.btnRegister.revertAnimation()
                        Toast.makeText(requireContext(), "successful register", Toast.LENGTH_LONG)
                            .show()
                    }

                    is Resource.Failure -> {
                        binding.btnRegister.revertAnimation()
                        Toast.makeText(
                            requireContext(),
                            resource.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()

                        Log.d(TAG, resource.message.toString())

                    }

                    is Resource.UnSpecified -> Unit
                }
            }
        }
    }

    private fun noInternetCallBack() {
        lifecycleScope.launchWhenStarted {
            viewModel.noInternet.collect {
                Toast.makeText(requireContext(), "No Inter net connection", Toast.LENGTH_LONG)
                    .show()

            }
        }
    }

    private fun validationState() {
        lifecycleScope.launchWhenStarted {
            viewModel.validationState.collect {
                if (it.email is ValidationState.Invalid)
                    binding.edEmail.error = it.email.msg

                if (it.password is ValidationState.Invalid)
                    binding.edpassword.error = it.password.msg

                if (it.firstName is ValidationState.Invalid)
                    binding.edFirstName.error = it.firstName.msg

                if (it.lastName is ValidationState.Invalid)
                    binding.edLastName.error = it.lastName.msg
            }
        }
    }

}