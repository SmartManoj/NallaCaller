package com.papputech.nallacaller
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>
                val messages = arrayOfNulls<SmsMessage>(pdus.size)
                for (i in pdus.indices) {
                    messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                }

                val sb = StringBuilder()
                for (message in messages) {
                    sb.append(message?.messageBody)
                }

                val sender = messages[0]?.originatingAddress

                // Handle the received SMS here
                // You can perform actions like displaying a notification, storing the message, etc.

                // For example, displaying a toast with the SMS content
//                Toast.makeText(context, "Received SMS from $sender: ${sb.toString()}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
