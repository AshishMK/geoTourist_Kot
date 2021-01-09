package com.x.geotourist.scenes.mainScene

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.Fragment
import com.x.geotourist.R
import com.x.geotourist.data.local.entity.TourDataEntity
import com.x.geotourist.scenes.mapScene.MapActivity
import com.x.geotourist.scenes.mapScene.MapFragment
import com.x.geotourist.scenes.readFileScene.ReadFileActivity
import com.x.geotourist.utils.Utils


class MainActivity : AppCompatActivity(), TourListListener {
    var fragment: Fragment? = null
    var hasTwoPanes = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hasTwoPanes = resources.getBoolean(R.bool.has_two_panes)
        if (hasTwoPanes) {
            fragment =
                supportFragmentManager.findFragmentById(R.id.mapFragment)
        }
        startPowerSaverIntent(this)
    }

    override fun onResume() {
        super.onResume()
        Utils.isGooglePlayServicesAvailable(this)

    }


    override fun onTourClicked(entity: TourDataEntity) {

        fragment?.let { it ->
            val mapFragment: MapFragment = it as MapFragment
            mapFragment.setTourIdFromActivity(entity.id as Long)

        } ?: run {
            startActivity(
                Intent(this, MapActivity::class.java).putExtra(
                    MapActivity.TOURID,
                    entity.id
                ).putExtra(MapActivity.TITLE, entity.title)
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.readFile) {
            checkPerMission()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startPowerSaverIntent(context: Context) {
        val settings =
            context.getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE)
        val skipMessage = settings.getBoolean("skipProtectedAppCheck", false)
        if (!skipMessage) {
            val editor = settings.edit()
            var foundCorrectIntent = false
            for (intent in Utils.POWERMANAGER_INTENTS) {
                if (Utils.isCallable(context, intent)) {
                    foundCorrectIntent = true
                    val dontShowAgain = AppCompatCheckBox(context)
                    dontShowAgain.setText(R.string.dont_show_again)
                    dontShowAgain.setOnCheckedChangeListener { _, isChecked ->
                        editor.putBoolean("skipProtectedAppCheck", isChecked)
                        editor.apply()
                    }
                    AlertDialog.Builder(context)
                        .setTitle(Build.MANUFACTURER + getString(R.string.protected_apps))
                        .setMessage(
                            String.format(
                                context.getString(R.string.autostart_msg),
                                context.getString(R.string.app_name)
                            )
                        )
                        .setView(dontShowAgain)
                        .setPositiveButton(R.string.go_to_setting
                        ) { dialog, which ->
                            try {
                                context.startActivity(intent)
                            } catch (se: SecurityException) {

                            }
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    break
                }
            }
            if (!foundCorrectIntent) {
                editor.putBoolean("skipProtectedAppCheck", true)
                editor.apply()
            }
        }
    }

    private fun checkPerMission() {
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val status: Utils.PermissionStatus = Utils.checkPermissions(this,
            Utils.PERMISSION_REQUEST,
            PERMISSIONS,
            null, R.string.storage_permission_msg
        )
        if (status === Utils.PermissionStatus.SUCCESS) {
            startActivity(Intent(this, ReadFileActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size == 1) {
            startActivity(Intent(this, ReadFileActivity::class.java))
        }
    }
}