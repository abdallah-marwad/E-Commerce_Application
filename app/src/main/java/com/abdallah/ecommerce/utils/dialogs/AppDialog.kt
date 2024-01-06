package com.abdallah.ecommerce.utils.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication
import java.util.Objects

class AppDialog {
        private lateinit var customDialog: AlertDialog

        fun showDialog(
            title: String,
            msg: String?,
            btnPosTxt: String?,
            btnNegTxt: String?,
            imgID: Int = -1,
            posListener: View.OnClickListener?,
            negListener: View.OnClickListener?
        ) {
            val dialogView: View = LayoutInflater.from(MyApplication.myAppContext.getCurrentAct())
                .inflate(R.layout.app_dialog, null)
            val dialogBuilder = AlertDialog.Builder(MyApplication.myAppContext.getCurrentAct())
            dialogBuilder.setView(dialogView)
            customDialog = dialogBuilder.create()
            customDialog.setCancelable(false)
            customDialog.setCanceledOnTouchOutside(false)
            customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            val btnPos = dialogView.findViewById<Button>(R.id.dialog_btn_pos)
            val btnNeg = dialogView.findViewById<Button>(R.id.dialog_btn_neg)
            val dialogTxtFail = dialogView.findViewById<TextView>(R.id.dialogTxt)
            val dialogTxtHint = dialogView.findViewById<TextView>(R.id.dialogTxtHint)
            val imgDialog = dialogView.findViewById<ImageView>(R.id.imgDialog)
            if (title == "") dialogTxtFail.visibility = View.GONE
            if (imgID == -1) imgDialog.visibility = View.GONE
            dialogTxtFail.text = title
            dialogTxtHint.text = msg
            btnPos.text = btnPosTxt
            btnNeg.text = btnNegTxt
            imgDialog.setImageResource(imgID)
            btnPos.setOnClickListener(posListener)
            btnNeg.setOnClickListener(negListener)
            customDialog.show()
        }
        fun dismiss(){
            if(customDialog?.isShowing == true)
                customDialog?.dismiss()
        }
    }

