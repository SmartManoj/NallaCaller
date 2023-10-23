package com.papputech.nallacaller


import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import android.telephony.SmsManager
import android.app.PendingIntent

class HeadlessSmsSendService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val phoneNumber = intent.getStringExtra("phone_number")
        val messageText = intent.getStringExtra("message_text")

        if (phoneNumber != null && messageText != null) {
            val smsManager = SmsManager.getDefault()
            val sentIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )

            smsManager.sendTextMessage(phoneNumber, null, messageText, sentIntent, null)
        }
    }

    companion object {
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, HeadlessSmsSendService::class.java, JOB_ID, work)
        }
    }
}
