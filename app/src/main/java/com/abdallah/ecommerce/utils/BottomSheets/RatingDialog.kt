package com.abdallah.ecommerce.utils.BottomSheets

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.abdallah.ecommerce.utils.validation.validateEmail
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wang.avi.AVLoadingIndicatorView

class RatingDialog{
    lateinit var  loader : AVLoadingIndicatorView
    lateinit var  btnCancel : AppCompatButton
    lateinit var  btnSend : AppCompatButton
    lateinit var  dialog : BottomSheetDialog
fun showDialog(
    onSendClick: (String, Float) -> Unit
) {
     dialog =
        BottomSheetDialog(MyApplication.myAppContext.getCurrentAct()!!, R.style.DialogStyle)
    val view = MyApplication.myAppContext.getCurrentAct()!!.layoutInflater.inflate(
        R.layout.rating_bottom_sheet,
        null
    )
    dialog.setContentView(view)
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    dialog.show()

    val itemRatingBar = view.findViewById<RatingBar>(R.id.itemRatingBar)
    val edComment = view.findViewById<EditText>(R.id.edOtp)
     btnCancel = view.findViewById(R.id.btnCancel)
     btnSend = view.findViewById(R.id.btnSend)
     loader = view.findViewById(R.id.loader)
    val warning = view.findViewById<ImageView>(R.id.warning)
    var rating = -1f

    btnCancel.setOnClickListener {
        dialog.dismiss()
    }

    itemRatingBar.setOnRatingBarChangeListener { p0, p1, p2 ->
        warning.visibility = View.GONE
        rating = p1
    }

    btnSend.setOnClickListener {
        if (rating == -1f) {
            warning.visibility = View.VISIBLE
            return@setOnClickListener
        }
        onSendClick(edComment.text.toString(), rating)
        dialog.dismiss()
    }
    fun showLoader(){
        warning.visibility = View.GONE
        loader.visibility= View.VISIBLE
        btnSend.visibility= View.INVISIBLE
        btnCancel.visibility= View.INVISIBLE
    }
    fun hideLoader(){
        loader.visibility= View.VISIBLE
        btnSend.visibility= View.VISIBLE
        btnCancel.visibility= View.VISIBLE
        warning.visibility = View.GONE

    }
    fun dismiss() {
        if(dialog!=null && dialog.isShowing)
            dialog.dismiss()
    }

}
}
