package com.abdallah.ecommerce.utils.BottomSheets

import android.app.Activity
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.utils.validation.validateInputAsNotEmpty
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class VerificationOtpBottomSheet(
) {

    fun createDialog(context: Activity, onSendClick: (String) -> Unit) {
        val dialog = BottomSheetDialog(context, R.style.DialogStyle)
        val view = context.layoutInflater.inflate(R.layout.otp_bottom_sheet, null)
        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()


        val edOtp = view.findViewById<EditText>(R.id.edOtp)
        val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnSend = view.findViewById<AppCompatButton>(R.id.btnSend)


        btnCancel.setOnClickListener {
            dialog.dismiss()
        }


        btnSend.setOnClickListener {

            val otp = edOtp.text.toString().trim()
            val isValid = validateInputAsNotEmpty(edOtp, "This field is required")


            if (isValid.not())
                return@setOnClickListener

            if (otp.length != 6) {
                edOtp.error = "Please Enter 6 digits"
                return@setOnClickListener
            }

            onSendClick(otp)
        }

    }
}