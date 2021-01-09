package com.x.geotourist.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.x.geotourist.R
import com.x.geotourist.application.AppController


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Service started after boot", Toast.LENGTH_LONG).show()
            ContextCompat.startForegroundService(
                context,
                Intent(AppController.getInstance(), BackgroundLocationService::class.java)
            )
        }
        else{
            Toast.makeText(context, R.string.storage_permission_msg, Toast.LENGTH_LONG).show()
        }
    }
}