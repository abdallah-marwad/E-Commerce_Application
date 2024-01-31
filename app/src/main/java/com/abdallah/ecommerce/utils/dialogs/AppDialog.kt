package com.abdallah.ecommerce.utils.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.ui.activity.LoginRegisterActivity

class AppDialog {
    private lateinit var customDialog: AlertDialog
    lateinit var progressDialog: Dialog

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

    fun dismiss() {
        if (customDialog?.isShowing == true)
            customDialog?.dismiss()
    }

    fun showingRegisterDialog(title: String, message: String) {
        showDialog(
            title,
            message,
            "LogIn",
            "Not Now",
            R.drawable.login,
            {
                dismiss()
                MyApplication.myAppContext.getCurrentAct()!!.startActivity(
                    Intent(
                        MyApplication.myAppContext.getCurrentAct(),
                        LoginRegisterActivity::class.java
                    )
                )
            },
            {
                dismiss()
            }
        )

    }

    fun showProgressDialog() {
        progressDialog = Dialog(MyApplication.myAppContext.getCurrentAct()!!)
        val inflate = LayoutInflater.from(MyApplication.myAppContext.getCurrentAct())
            .inflate(R.layout.loading_dialog, null)
        progressDialog.setContentView(inflate)
        progressDialog.setCancelable(false)
        progressDialog.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        if (MyApplication.myAppContext.getCurrentAct()!!.isFinishing.not())
            progressDialog.show()
    }
    fun dismissProgress(){
        if (progressDialog != null )
            if( progressDialog.isShowing )
                progressDialog.dismiss()
    }

}

