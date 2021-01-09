package com.x.geotourist.scenes.readFileScene

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.x.geotourist.R
import com.x.geotourist.databinding.ReadFileActivityBinding
import com.x.geotourist.services.BackgroundLocationService
import com.x.geotourist.utils.Utils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference


class ReadFileActivity : AppCompatActivity() {
    lateinit var weakActivity: WeakReference<Activity>

    /**
     * I am using Data binding
     * */
    private lateinit var binding: ReadFileActivityBinding

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        weakActivity = WeakReference(this)
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
        initialiseView()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /*
    * Initialising the View using Data Binding
    * */
    private fun initialiseView() {

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_read_file
        )
        readFile()
    }


    private fun readFile() {
        object : AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?): Unit {
                try {
                    val myFile = File(Utils.getDirectory(), "data.txt")
                    val fIn = FileInputStream(myFile)
                    val myReader = BufferedReader(InputStreamReader(fIn))
                    var aDataRow: String? = ""
                    var aBuffer: String? = ""
                    while (myReader.readLine().also { aDataRow = it } != null) {
                        aBuffer += aDataRow + "\n"
                    }
                    if (weakActivity.get() != null) {
                        runOnUiThread {
                            binding.content = aBuffer
                        }

                    }
                    myReader.close()
                } catch (e: Exception) {

                }

            }

        }.execute()

    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                BackgroundLocationService.ACTION_FILE_READ_BROADCAST
            )
        )
    }

    /**
     * Receiver for location broadcasts from [BackgroundLocationService].
     */
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            readFile()
        }
    }
}