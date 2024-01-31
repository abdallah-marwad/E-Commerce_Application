package com.abdallah.ecommerce.ui.fragment.shopping.cart.address.addAddress

import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.application.core.BaseFragment
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.databinding.FragmentAdddAdressBinding
import com.abdallah.ecommerce.utils.BottomSheets.SingleInputBottomSheet
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.abdallah.ecommerce.utils.permissions.LocationPermission
import com.abdallah.ecommerce.utils.validation.isInputNotEmpty
import com.abdallah.ecommerce.utils.validation.isPhoneNumberValid
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
class AddAddressFragment : BaseFragment<FragmentAdddAdressBinding>() {
    private val viewModel by viewModels<AddingAddressViewModel>()
    private var otpBottomSheet: SingleInputBottomSheet? = null
    private var phoneBottomSheet: SingleInputBottomSheet? = null
    private var phoneNumber: String = ""
    private lateinit var addressModel: AddressModel
    private var job: Job = Job()
    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }
    val locationPermission by lazy { LocationPermission() }

    @Inject
    lateinit var firebaseAuth: FirebaseAuth


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        addAddressesCallBack()
        observeOtpState()
        observeAddingPhone()
        noInternetCallback()
        locationCallback()
        fragOnClick()
    }

    private fun locationCallback() {
        locationPermission.locationListener = object : LocationPermission.OnLocationDetected {
            override fun locationDetected(location: Location) {
                setAddress(location.latitude, location.longitude)
            }
        }
    }

    private fun noInternetCallback() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noInternet.collect {
                    showShortSnackBar("No internet connection")
                }
            }
        }
    }

    private fun initViews() {
        val phoneNumber = firebaseAuth.currentUser?.phoneNumber
        if (!phoneNumber.isNullOrEmpty()) {
            binding.edPhone.text = phoneNumber
            binding.edPhone.isEnabled = false
        }
        binding.toolbar.title.text = "Add Address"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun fragOnClick() {
        binding.edPhone.setOnClickListener {
            showAddPhoneBottomSheet()
        }
        binding.toolbar.cardImage.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnAddNewAddress.setOnClickListener {
            addAddress()
        }
        binding.currentLocation.setOnClickListener {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        locationPermission.takeLocationPermission(requireActivity())
    }

    private fun setAddress(MyLat: Double, MyLong: Double) {
        job.cancel()
        job = coroutineScope.launch {
            try {
                val geocoder = Geocoder(requireContext(), Locale.ENGLISH)
                val addresses = geocoder.getFromLocation(MyLat, MyLong, 1)
                if (addresses!!.isNotEmpty()) {
                    val cityName = addresses[0].locality
                    val street = addresses[0].getAddressLine(0)
                    val state = addresses[0].adminArea
                    withContext(Dispatchers.Main) {
                        binding.edAddressTitle.setText(street)
                        binding.edStreet.setText(street)
                        binding.edState.setText(state.toString())
                        binding.edCity.setText(cityName)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }


    private fun addAddress() {
        if (firebaseAuth.currentUser == null) {
            AppDialog().showingRegisterDialog(
                Constant.COULDNOT_DO_THIS_ACTON,
                Constant.PLS_LOGIN
            )
            return
        }
        if (isDataValid().not()) {
            showInputsErrMsg()
            return
        }
        val id = UUID.randomUUID().toString()
        addressModel = AddressModel(
            id,
            binding.edAddressTitle.text.toString(),
            binding.edFullName.text.toString(),
            binding.edStreet.text.toString(),
            binding.edPhone.text.toString(),
            binding.edCity.text.toString(),
            binding.edState.text.toString(),
            false
        )
        viewModel.addAddresses(
            id,
            firebaseAuth.currentUser!!.uid,
            addressModel
        )
    }

    private fun addAddressesCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addAddress.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            hideProgressDialog()
                            findNavController()
                                .previousBackStackEntry?.savedStateHandle?.set(
                                    Constant.ADDRESS,
                                    addressModel
                                )
                            findNavController().popBackStack()
                        }

                        is Resource.Failure -> {
                            hideProgressDialog()
                            showLongToast(result.message ?: "")
                        }

                        is Resource.Loading -> {
                            try {
                                showProgressDialog()
                            } catch (e: Exception) {
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SuspiciousIndentation")
    private fun showOtpBottomSheet(verificationId: String) {
        otpBottomSheet = SingleInputBottomSheet()
        otpBottomSheet!!.createDialog(
            requireActivity(),
            "Enter Otp",
            "Verify",
            "Otp Verification",
            "Enter The Verification Otp sent to you"
        ) { otp, ed ->
            if (otp.length != 6) {
                ed.error = "Please Enter 6 digits"
                return@createDialog
            }
            viewModel.updatePhoneNumber(verificationId, otp)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showAddPhoneBottomSheet() {
        phoneBottomSheet = SingleInputBottomSheet()
        phoneBottomSheet!!.createDialog(
            requireActivity(),
            "Enter Phone Number",
            "Send Otp",
            "Add phone number",
            "enter your phone number without country code"
        )
        { phoneNumber, ed ->
            if (isPhoneNumberValid(ed)) {
                viewModel.sendVerificationCode(phoneNumber, requireActivity())
                this.phoneNumber = phoneNumber
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observeAddingPhone() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updatePhone.collect {
                    when (it) {
                        is Resource.Success -> {
                            Toast.makeText(
                                context,
                                "Successful Add phone number",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.edPhone.text = phoneNumber
                            binding.edPhone.isEnabled = false
                            otpBottomSheet?.dismiss()
                        }

                        is Resource.Failure -> {
                            otpBottomSheet?.hideLoader()
                            Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                        }

                        is Resource.Loading -> {
                            otpBottomSheet?.showLoader()
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observeOtpState() {
        viewModel.sendOtpState.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    phoneBottomSheet?.dismiss()
                    showOtpBottomSheet(it.data!!)
                }

                is Resource.Failure -> {
                    phoneBottomSheet?.hideLoader()
                    Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    phoneBottomSheet?.showLoader()
                }

                else -> {}
            }
        }
    }

    private fun isDataValid() = binding.edPhone.isEnabled.not() &&
            binding.edAddressTitle.text.isNotEmpty() &&
            binding.edFullName.text.isNotEmpty() &&
            binding.edState.text.isNotEmpty() &&
            binding.edCity.text.isNotEmpty() &&
            binding.edStreet.text.isNotEmpty()


    private fun showInputsErrMsg() {
        if (binding.edPhone.isEnabled)
            showShortSnackBar("Phone is required")
        isInputNotEmpty(binding.edAddressTitle, Constant.REQUIRED_FIELD)
        isInputNotEmpty(binding.edFullName, Constant.REQUIRED_FIELD)
        isInputNotEmpty(binding.edCity, Constant.REQUIRED_FIELD)
        isInputNotEmpty(binding.edState, Constant.REQUIRED_FIELD)
        isInputNotEmpty(binding.edStreet, Constant.REQUIRED_FIELD)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(LOCATION_SERVICE) as LocationManager?
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("test", "onActivityResult add frag")

        if (requestCode == Constant.REQUEST_LOCATION_SETTINGS) {
            if (isLocationEnabled().not()) {
                showLongToast("Location is still not enabled.")
                return
            }
            locationPermission.detectLocation(requireActivity())
        }
    }


    override fun onStop() {
        super.onStop()
        job.cancel()
    }
}