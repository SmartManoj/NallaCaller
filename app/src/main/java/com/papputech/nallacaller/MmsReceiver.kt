package com.papputech.nallacaller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.util.Log

class MmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION) {
            val mimeType = intent.type
            if (mimeType != null && mimeType.startsWith("application/vnd.wap.mms-message")) {
                val bundle = intent.extras
                if (bundle != null) {
                    val data = bundle.getByteArray("data")
                    if (data != null) {
                        // Process the MMS data here
                        // You can decode and handle the multimedia content as needed

                        // For example, you can log the MMS received
                        Log.d("MmsReceiver", "Received MMS with type: $mimeType")
                    }
                }
            }
        }
    }
}
