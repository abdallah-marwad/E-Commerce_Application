package com.abdallah.ecommerce.utils.BottomSheets

import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.utils.validation.isInputNotEmpty
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wang.avi.AVLoadingIndicatorView

class SingleInputBottomSheet() {
    private lateinit var dialog :BottomSheetDialog
    private lateinit var loader :AVLoadingIndicatorView
    private lateinit var btnSend :AppCompatButton
    private lateinit var btnCancel :AppCompatButton
    fun createDialog(context: Activity, edHint : String,btnTxt : String,titleTxt : String,hintTxt : String ,onSendClick: (String , EditText) -> Unit) {
        dialog = BottomSheetDialog(context, R.style.DialogStyle)
        val view = context.layoutInflater.inflate(R.layout.otp_bottom_sheet, null)
        dialog.setContentView(view)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()


        val edOtp = view.findViewById<EditText>(R.id.edOtp)
        val hint = view.findViewById<TextView>(R.id.hint)
        val title = view.findViewById<TextView>(R.id.title)
         btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        btnSend = view.findViewById<AppCompatButton>(R.id.btnSend)
         loader = view.findViewById<AVLoadingIndicatorView>(R.id.loader)

        edOtp.hint = edHint
        btnSend.text = btnTxt
        title.text = titleTxt
        hint.text = hintTxt


        btnCancel.setOnClickListener {
            dialog.dismiss()
        }


        btnSend.setOnClickListener {

            val otp = edOtp.text.toString().trim()
            val isValid = isInputNotEmpty(edOtp, "This field is required")


            if (isValid.not())
                return@setOnClickListener


            onSendClick(otp , edOtp)
        }

    }
    fun dismiss() {
        if(dialog.isShowing)
            dialog.dismiss()
    }
    fun showLoader() {
        loader.visibility = View.VISIBLE
        btnCancel.visibility = View.GONE
        btnSend.visibility = View.GONE
    }
    fun hideLoader() {
        loader.visibility = View.GONE
        btnCancel.visibility = View.VISIBLE
        btnSend.visibility = View.VISIBLE
    }
}