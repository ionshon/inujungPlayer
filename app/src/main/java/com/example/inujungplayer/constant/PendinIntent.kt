package com.example.inujungplayer.constant

import android.content.Intent
import com.example.inujungplayer.service.ForegroundService
import com.example.inujungplayer.utils.MyApplication

object PendinIntent {
    val lPauseIntent = Intent(MyApplication.applicationContext(), ForegroundService::class.java)
    val lPlayIntent = Intent(MyApplication.applicationContext(), ForegroundService::class.java)
    val lReplayIntent = Intent(MyApplication.applicationContext(), ForegroundService::class.java)

    var intent: Intent? = null

}