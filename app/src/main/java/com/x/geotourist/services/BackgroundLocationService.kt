/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.x.geotourist.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.x.geotourist.R
import com.x.geotourist.scenes.mainScene.MainActivity
import com.x.geotourist.utils.Utils
import com.x.geotourist.utils.toText
import timber.log.Timber
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 *
 * For apps running in the background on O+ devices, location is computed much less than previous
 * versions. Please reference documentation for details.
 */
class BackgroundLocationService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 12345678
        private const val REFRESH_TIME : Long = 1000 * 60 * 14
        private const val PACKAGE_NAME = "com.x.geotourist."
        internal const val ACTION_FILE_READ_BROADCAST =
            "$PACKAGE_NAME.action.ACTION_FILE_READ_BROADCAST"
        private const val EXTRA_CANCEL_LOCATION_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.EXTRA_CANCEL_LOCATION_FROM_NOTIFICATION"
    }

    lateinit var refresher: TimerTask
    lateinit var timer: Timer

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null
    private lateinit var notificationManager: NotificationManager
    override fun onCreate() {

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Initialization code in onCreate or similar:

        // Initialization code in onCreate or similar:
        timer = Timer()
        refresher = object : TimerTask() {
            override fun run() {
                Thread(runnable).start()
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = TimeUnit.SECONDS.toMillis(30)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = TimeUnit.SECONDS.toMillis(15)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // TODO: Step 1.4, Initialize the LocationCallback.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (locationResult?.lastLocation != null) {

                    // Normally, you want to save a new location to a database. We are simplifying
                    // things a bit and just saving it as a local variable, as we only need it again
                    // if a Notification is created (when the user navigates away from app).
                    currentLocation = locationResult.lastLocation

                    // Notify our Activity that a new location was added. Again, if this was a
                    // production app, the Activity would be listening for changes to a database
                    // with new locations, but we are simplifying things a bit to focus on just
                    // learning the location side of things.
                    /*      val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                          intent.putExtra(EXTRA_LOCATION, currentLocation)
                          LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)*/
                    /*      notificationManager.notify(
                              NOTIFICATION_ID,
                              generateForeGroundNotification(currentLocation)
                          )*/

                }
            }
        }
        subscribeToLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }


    override fun onDestroy() {
        unsubscribeToLocationUpdates()
    }

    private fun hasPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            writeContent("No Location found at " + Utils.sdf.format(Date().time))
            return false
        }
        return true
    }

    private fun subscribeToLocationUpdates() {
        if (!hasPermission()) {
            return
        }
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.myLooper()
            )
            startForeground(NOTIFICATION_ID, generateForeGroundNotification(null))
            // first event immediately,  following after 10 seconds each
            timer.scheduleAtFixedRate(refresher, 0, REFRESH_TIME)
        } catch (unlikely: SecurityException) {
            stopForeground(true)
            stopSelf()

            println("location df  ex" + (unlikely.localizedMessage))
        }
    }

    private fun unsubscribeToLocationUpdates() {
        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                } else {
                }
            }

        } catch (unlikely: SecurityException) {
        }
        refresher.cancel()

        stopForeground(true)
    }

    private fun generateForeGroundNotification(location: Location?): Notification {
        val mainNotificationText =
            (location?.toText() ?: getString(R.string.location_started)) + "\n" + Utils.sdf.format(
                Date().time
            )
        val titleText = getString(R.string.app_name)
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val cancelIntent = Intent(this, BackgroundLocationService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_FROM_NOTIFICATION, true)

        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "1")
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.drawable.ic_baseline_location_on_24)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_baseline_play_arrow_24,
                getString(R.string.stop_location_updates_button_text),
                servicePendingIntent
            )
            .build()
    }

    private val runnable: Runnable = Runnable()
    {
        writeFile()
    };

    private fun getFileLineNumber(file: File): Int {
        val inputStream: InputStream = FileInputStream(file)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val lineNumberReader = LineNumberReader(bufferedReader)
        try {
            lineNumberReader.skip(Long.MAX_VALUE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Timber.v("file writed " + lineNumberReader.lineNumber)
        return lineNumberReader.lineNumber + 1

    }

    @SuppressLint("MissingPermission")
    private fun writeFile() {
        if (!hasPermission()) {
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                location?.let {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateForeGroundNotification(location!!)
                    )


                }


                val txt = location?.toText(
                    Utils.sdf.format(
                        Date().time
                    )
                )
                Toast.makeText(
                    this@BackgroundLocationService,
                    txt ?: "No Location found at " + Utils.sdf.format(Date().time),
                    Toast.LENGTH_LONG
                ).show()
                writeContent(txt ?: "No Location found at " + Utils.sdf.format(Date().time))
            }

    }

    private fun writeContent(content: String) {
        try {
            val myFile = File(Utils.getDirectory(), "data.txt")
            if (!myFile.exists()) {
                myFile.createNewFile()
            }
            var createNewFile = getFileLineNumber(myFile)
            if (createNewFile > 10)
                myFile.createNewFile()
            val fOut = FileOutputStream(myFile, createNewFile < 11)

            val myOutWriter = OutputStreamWriter(fOut)
            val lineNumber = if (createNewFile > 10) 1 else createNewFile
            myOutWriter.append("$lineNumber: $content\n")
            myOutWriter.close()
            fOut.close()
            Timber.v("file writed")

        } catch (e: Exception) {
            Timber.v("file writed " + e.localizedMessage)
        }
        val intent = Intent(ACTION_FILE_READ_BROADCAST)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

    }


}
