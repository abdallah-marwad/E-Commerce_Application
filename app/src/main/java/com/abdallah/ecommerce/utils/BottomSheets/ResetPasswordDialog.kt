package com.abdallah.ecommerce.utils.BottomSheets

import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.abdallah.ecommerce.utils.validation.validateEmail
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


fun Fragment.showResetPasswordDialog(
    onSendClick: (String, Boolean, String) -> Unit
) {
    val dialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
    val view = layoutInflater.inflate(R.layout.reset_pass_dialog, null)
    dialog.setContentView(view)
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    dialog.show()


    val edEmail = view.findViewById<EditText>(R.id.ed_email_login)
    val btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
    val btnSend = view.findViewById<AppCompatButton>(R.id.btnSend)


    btnCancel.setOnClickListener {
        dialog.dismiss()
    }


    btnSend.setOnClickListener {

        val email = edEmail.text.toString().trim()
        val emailState = validateEmail(email)
        val validEmail = emailState is ValidationState.Valid
        var errMsg = ""

        if (!validEmail)
            errMsg = emailState.msg

        onSendClick(email, validEmail, errMsg)

        dialog.dismiss()
    }


}