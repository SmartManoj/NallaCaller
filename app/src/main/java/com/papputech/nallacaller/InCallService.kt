package com.papputech.nallacaller

import android.app.Service
import android.content.Intent
import android.os.IBinder

// This is needed in order to allow choosing <my app> as default Phone app

class InCallService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

}