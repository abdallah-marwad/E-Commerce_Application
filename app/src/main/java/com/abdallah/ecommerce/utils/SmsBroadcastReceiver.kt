package com.abdallah.ecommerce.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern


class SmsBroadcastReceiver : BroadcastReceiver() {

    private var otpListener: OTPReceiveListener? = null

    fun setOTPListener(otpListener: OTPReceiveListener?) {
        this.otpListener = otpListener
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    var message: String? = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                    message?.let {
//                        // val p = Pattern.compile("[0-9]+") check a pattern with only digit
//                        val p = Pattern.compile("\\d+")
//                        val m = p.matcher(it)
//                        if (m.find()) {
//                            val otp = m.group()
                        if (otpListener != null) {
                            if (it.contains("ecommerceapplication-d2a56.firebaseapp.com"))
                                otpListener!!.onOTPReceived(it.substring(0, 5))
                        }
//                        }

                    }
                }

                CommonStatusCodes.TIMEOUT -> {}
            }
        }
    }

    interface OTPReceiveListener {
        fun onOTPReceived(otp: String?)
    }
}