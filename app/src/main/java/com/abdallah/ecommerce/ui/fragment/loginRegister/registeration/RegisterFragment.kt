package com.abdallah.ecommerce.ui.fragment.loginRegister.registeration

import android.annotation.SuppressLint
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.User
import com.abdallah.ecommerce.data.sharedPreferences.SharedPreferencesHelper
import com.abdallah.ecommerce.databinding.FragmentRegisterBinding
import com.abdallah.ecommerce.ui.activity.ShoppingActivity
import com.abdallah.ecommerce.utils.BottomSheets.SingleInputBottomSheet
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Constant.IS_SKIP
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.abdallah.ecommerce.utils.validation.isPhoneNumberValid
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.AndroidEntryPoint

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
        SharedPreferencesHelper.addBoolean(Constant.NOT_FIRST_TIME,true)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arlInitial()
        userAndPassRegisterCallBack()
        noInternetCallBack()
        googleSignInCallBack()
        fragOnClicks()
        validationState()

    }



    private fun arlInitial() {
        arl = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
           try {
               lifecycleScope.launchWhenStarted {
                       val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
                       account?.let { googleAccount ->
                           viewModel.googleAuthWithFireBase(googleAccount)
                       }?:hideLoader()
               }
           }catch (e: Exception) {
               hideLoader()
               Toast.makeText(context,"please try again",Toast.LENGTH_LONG).show()
           }

    }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun googleSignInRequest() {
        lifecycleScope.launchWhenStarted {
            val signInClient = viewModel.googleSignInRequest(requireActivity())
            signInClient?.let {googleClient->
                googleClient.apply {
                    arl.launch(signInIntent)
                }
            }?:hideLoader()
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
                )
            }


            phoneLogin.setOnClickListener {
                showRegisterPhoneBottomSheet()
            }


            binding.gmailLogin.setOnClickListener {
                showLoader()
                googleSignInRequest()
            }

            skip.setOnClickListener{
                SharedPreferencesHelper.addBoolean(IS_SKIP,true)
                startActivity(Intent(context , ShoppingActivity::class.java))
                requireActivity().finish()
            }

        }
    }



    @SuppressLint("SuspiciousIndentation")
    private fun showOtpBottomSheet() {

        val bottomSheet = SingleInputBottomSheet()
            bottomSheet.createDialog(requireActivity() ,
                "Enter Otp" ,
                "Verify",
                "Otp Verification",
                "Enter The Verification Otp sent to you"
            ) {otp , ed->
            if (otp.length != 6) {
                ed.error = "Please Enter 6 digits"
                return@createDialog
            }
            viewModel.registerWithPhone(otp)
            observeOtpState(bottomSheet)
        }
    }
    private fun observeOtpState(bottomSheet : SingleInputBottomSheet) {
        viewModel.sendOtpState.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    bottomSheet.dismiss()
                    Toast.makeText(requireContext(), "successful register", Toast.LENGTH_LONG)
                        .show()
                    SharedPreferencesHelper.addBoolean(Constant.IS_LOGGED_IN,true)
                    startActivity(Intent(context , ShoppingActivity::class.java))
                    activity?.finish()
                }

                is Resource.Failure -> {
                    bottomSheet.dismiss()
                    Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    bottomSheet.showLoader()
                }

                else -> {}
            }
        }
    }
    private fun showRegisterPhoneBottomSheet() {

        val phoneBottomSheet = SingleInputBottomSheet()
        phoneBottomSheet.createDialog(
            requireActivity(),
            "Enter Phone Number" ,
            "Send Otp",
            "Register with phone number",
            "enter your phone number for registration"
        )
        {phoneNumber , ed->
            if(isPhoneNumberValid(ed)){
                viewModel.sendVerificationCode(phoneNumber , requireActivity())
            }
            observeRegisterPhone(phoneBottomSheet)


        }
    }
    private fun observeRegisterPhone(bottomSheet : SingleInputBottomSheet) {
        viewModel.phoneRegisterState.observe(viewLifecycleOwner) {

            when (it){
                is Resource.Success ->{
                    bottomSheet.dismiss()
                    showOtpBottomSheet()
                }
                is Resource.Failure ->{
                    bottomSheet.dismiss()
                    Toast.makeText(activity , it.message , Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading ->{
                    bottomSheet.showLoader()
                }

                else -> {

                }
            }


        }
    }

    private fun userAndPassRegisterCallBack() {

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
                        SharedPreferencesHelper.addBoolean(Constant.IS_LOGGED_IN,true)

                        startActivity(Intent(context , ShoppingActivity::class.java))
                        activity?.finish()
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
                hideLoader()
                Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_LONG)
                    .show()

            }
        }
    }
    private fun googleSignInCallBack() {
        lifecycleScope.launchWhenStarted {
            viewModel.googleRegister.collect{
                when (it){
                    is Resource.Loading -> {
                        showLoader()
                    }
                    is Resource.Success -> {
                        Toast.makeText(context , "Successful signin" , Toast.LENGTH_SHORT).show()
                        hideLoader()
                        SharedPreferencesHelper.addBoolean(Constant.IS_LOGGED_IN,true)

                        startActivity(Intent(context , ShoppingActivity::class.java))
                        activity?.finish()
                    }
                    is Resource.Failure -> {
                        Toast.makeText(context , "fail to signin " , Toast.LENGTH_SHORT).show()
                        hideLoader()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showLoader() {
        binding.loader.visibility = View.VISIBLE
        binding.linearLayout.visibility = View.INVISIBLE
    }
    private fun hideLoader() {
        binding.loader.visibility = View.INVISIBLE
        binding.linearLayout.visibility = View.VISIBLE
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