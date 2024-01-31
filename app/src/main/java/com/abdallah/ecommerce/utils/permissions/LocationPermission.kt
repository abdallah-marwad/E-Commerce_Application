package com.abdallah.ecommerce.utils.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*

class LocationPermission {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationListener: OnLocationDetected
    fun takeLocationPermission(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val dialog = AppDialog()

            dialog.showDialog(
                "Location Permission",
                "To get current location you should allow location permission",
                "Ok",
                "Cancel",
                R.drawable.location_vector,
                {
                    showPermission(activity)
                    dialog.dismiss()
                },
                { dialog.dismiss() }
            )
        } else {
            detectLocation(activity)
        }

    }

    private fun showPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            Constant.LOCATION_REQUEST_CODE
        )
    }


    @SuppressLint("MissingPermission")
    fun detectLocation(context: Activity) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    val dialog = AppDialog()
                    dialog.showDialog(
                        "Location Setting",
                        "Go to setting to open location",
                        "Go",
                        "Cancel",
                        R.drawable.location_vector,
                        {
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            ActivityCompat.startActivityForResult(
                                MyApplication.myAppContext.getCurrentAct()!!,
                                intent, Constant.REQUEST_LOCATION_SETTINGS, null
                            )
                            dialog.dismiss()
                        },
                        { dialog.dismiss() })
                    return@addOnSuccessListener
                }
                locationListener.locationDetected(location)
            }
    }

    interface OnLocationDetected {
        fun locationDetected(location: Location)
    }

}


