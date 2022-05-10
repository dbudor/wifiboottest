package com.zipato.wifiboottest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //       val isSDPresent = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        //       val isSDSupportedDevice = Environment.isExternalStorageRemovable()
        //       if (isSDPresent && isSDSupportedDevice) {
        intent.setClass(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        context.startActivity(intent)
        //        }
    }
}