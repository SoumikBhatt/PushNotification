package com.soumik.forceupdate2.classes

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.soumik.forceupdate2.networkflow.models.CheckVersionBody
import com.soumik.forceupdate2.R
import com.soumik.forceupdate2.networkflow.api.WebService
import com.soumik.forceupdate2.networkflow.models.CheckVersionResponse
import com.soumik.utilslibrary.Utills

class ForceUpdate {

    companion object{

        fun checkVersion(context: Context,appID:Int,versionCode:String,appName: String,appIcon: Int):String{

            val checkVersionBody = CheckVersionBody()
            checkVersionBody.app_id = appID
            checkVersionBody.version_code = versionCode

            return checkVersionFromApi(context,checkVersionBody,appName, appIcon)
        }

        private fun checkVersionFromApi(context: Context, checkVersionBody: CheckVersionBody, appName:String, appIcon:Int):String {

            var status = ""

            WebService.callCheckVersionAPI(checkVersionBody){ response: CheckVersionResponse?, error: String? ->
                if (error==null){
                    if (response?.success=="true"){
                        status = when (response.details.status) {
                            1 -> {
                                //active
                                "active"
                            }
                            2 -> {
                                //deprecated
                                showDeprecatedDialog(context,appName,appIcon)
                            }
                            else -> {
                                //expired
                                showExpiredDialog(context,appName,appIcon)
                            }
                        }
                    } else Log.d(TAG,"Check Version success is ${response?.success}")
                } else Log.d(TAG,"Check Version Failed...")
            }

            return status
        }

        @SuppressLint("SetTextI18n")
        private fun showDeprecatedDialog(context: Context, appName: String, appIcon: Int):String {
            var deprecatedStatus = ""
            val dialog = Dialog(context,android.R.style.Theme_Light_NoTitleBar_Fullscreen).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(R.layout.dialog_expired)
            }

            val iconIV = dialog.findViewById<ImageView>(R.id.iv_icon_deprecated)
            val textTV = dialog.findViewById<TextView>(R.id.tv_dialog_deprecated_text)
            val instructionTV = dialog.findViewById<TextView>(R.id.tv_dialog_deprecated_inst)
            val updateBtn = dialog.findViewById<Button>(R.id.btn_deprecated_update)
            val remindLaterBtn = dialog.findViewById<Button>(R.id.btn_deprecated_remind_later)
            val notShowCheck = dialog.findViewById<CheckBox>(R.id.check_donT_show)

            iconIV.setImageResource(appIcon)
            instructionTV.text = "A new version of $appName is available"

            updateBtn.setOnClickListener {
                deprecatedStatus = "update"
                Utills.rateApp(context)
                dialog.dismiss()
            }

            remindLaterBtn.setOnClickListener {
                deprecatedStatus = "remindLater"
                dialog.dismiss()
            }

            deprecatedStatus += if (notShowCheck.isChecked) " don'tShow"
            else " show"

            return deprecatedStatus
        }

        @SuppressLint("SetTextI18n")
        private fun showExpiredDialog(context: Context, appName: String, appIcon: Int):String {
            var expiredStatus = ""
            val dialog = Dialog(context,android.R.style.Theme_Light_NoTitleBar_Fullscreen).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(R.layout.dialog_expired)
            }

            val iconIV = dialog.findViewById<ImageView>(R.id.iv_icon_expired)
            val textTV = dialog.findViewById<TextView>(R.id.tv_dialog_expired_text)
            val instructionTV = dialog.findViewById<TextView>(R.id.tv_dialog_expired_inst)
            val updateBtn = dialog.findViewById<Button>(R.id.btn_expired_update)

            iconIV.setImageResource(appIcon)
            textTV.text = "$appName needs an update"

            updateBtn.setOnClickListener {
                expiredStatus = "update expired"
                Utills.rateApp(context)
                dialog.dismiss()
            }

            dialog.show()

            return expiredStatus
        }

       private const val TAG = "FORCE UPDATE"
    }


}