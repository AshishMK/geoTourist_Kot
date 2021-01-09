package com.x.geotourist.utils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.x.geotourist.R
import com.x.geotourist.application.AppController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Returns the `location` object as a human readable string.
 */
fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

/**
 * Returns the `location` object as a human readable string with time.
 */
fun Location?.toText(time: String): String {
    return if (this != null) {
        "latitude: $latitude, longitude: $longitude, time: $time"
    } else {
        "Unknown location , time: $time"
    }
}

class Utils {
    companion object {
        const val PICK_CAMERA_VID_REQUEST = 1005
        var colors = intArrayOf(Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN)
        const val PERMISSION_REQUEST = 101
        var sdf = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss")

        public fun isGooglePlayServicesAvailable(activity: Activity?): Boolean {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
            if (status != ConnectionResult.SUCCESS) {
                if (googleApiAvailability.isUserResolvableError(status)) {
                    googleApiAvailability.getErrorDialog(activity, status, 1000).show()
                } else {
                    Toast.makeText(
                        AppController.getInstance(),
                        "Google Services not available",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return false
            }
            return true
        }

        /**
         * Return the resource directory of the application
         *
         * @return
         */
        fun getCacheDirectory(): File {
            val f = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/" + AppController.getInstance()
                    .getString(R.string.app_name) + "/.cache"
            )
            f.mkdirs()
            return f
        }

        fun getDirectory(): File {
            val f = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/" + AppController.getInstance()
                    .getString(R.string.app_name)
            )
            f.mkdirs()
            return f
        }

        fun getFileName(ext: String): String {
            return sdf.format(Date().time) + "_kot" + ext
        }


        /* Create the NotificationChannel, but only on API 26+ because
    the NotificationChannel class is new and not in the support library
   */
        fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name: CharSequence =
                    AppController.getInstance().getString(R.string.notification_channel)
                val att = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val description =
                    AppController.getInstance().getString(R.string.notification_channel_msg)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel("1", name, importance)
                val attributes =
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager =
                    AppController.getInstance().getSystemService(
                        NotificationManager::class.java
                    )
                notificationManager.createNotificationChannel(channel)

                //Silent notification channel
                val NOTIFICATION_CHANNEL_ID = "2"
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    AppController.getInstance().getString(R.string.silent_notification),
                    NotificationManager.IMPORTANCE_LOW
                )
                //Configure the notification channel, NO SOUND
                notificationChannel.description =
                    AppController.getInstance().getString(R.string.silent_notification_msg)
                notificationChannel.setSound(null, null)
                notificationChannel.enableVibration(false)
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        /**
         * Method to perform Permission model request response operations for the app with
         * fallback functionality [Utils.openSettingApp]
         *
         * @param activity
         * @param PERMISSION_REQUEST_CODE
         * @param fragment
         * @return
         */
        fun checkPermissions(
            activity: Activity,
            PERMISSION_REQUEST_CODE: Int,
            permissions: Array<String>,
            fragment: Fragment?,
            msgStringId: Int
        ): PermissionStatus {
            val unGrantedPermissions =
                ArrayList<String>()
            var shouldShowRequestPermissionRationale = false
            for (i in permissions.indices) {
                if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    unGrantedPermissions.add(permissions[i])
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            permissions[i]
                        )
                    ) {
                        shouldShowRequestPermissionRationale = true
                    }
                }
            }
            if (unGrantedPermissions.size > 0) {
                if (shouldShowRequestPermissionRationale) {
                    Toast.makeText(activity, msgStringId, Toast.LENGTH_LONG).show()
                    openSettingApp(activity)
                    return PermissionStatus.ERROR
                }
                if (fragment == null) {
                    ActivityCompat.requestPermissions(
                        activity,
                        permissions, PERMISSION_REQUEST_CODE
                    )
                    return PermissionStatus.REQUESTED
                } else {
                    fragment.requestPermissions(permissions, PERMISSION_REQUEST_CODE)
                    return PermissionStatus.REQUESTED
                }
            } else {
                return PermissionStatus.SUCCESS
            }
        }

        /**
         * return bitmap from vector drawables
         * ((BitmapDrawable) AppCompatResources.getDrawable(getTarget().getContext(), R.drawable.ic_thin_arrowheads_pointing_down)).getBitmap()
         */
        @JvmStatic
        fun getBitmapFromVectorDrawable(
            context: Context,
            drawableId: Int, color: Int
        ): Bitmap {
            var drawable = ContextCompat.getDrawable(context!!, drawableId)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = DrawableCompat.wrap(drawable!!).mutate()
            }
            DrawableCompat.setTint(drawable!!, colors[color])
            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            //canvas.drawBitmap(bitmap, 0f, 0f, paint)
            return bitmap
        }

        /**
         * Method to open App's Settings info screen to manually revoke permissions
         * its a fallback for permission model
         *
         * @param ctx
         */
        private fun openSettingApp(ctx: Context) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse(
                "package:" + AppController.getInstance().packageName
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            ctx.startActivity(intent)
        }

        fun pickVideoFromCameraFragment(frg: Fragment): String {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            val f = File(getDirectory(), getFileName(".mp4"))
            val uri = FileProvider.getUriForFile(
                AppController.getInstance(),
                AppController.getInstance().packageName.toString() + ".provider",
                f
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            frg.startActivityForResult(
                intent, PICK_CAMERA_VID_REQUEST
            )
            return f.absolutePath
        }


        var POWERMANAGER_INTENTS = Arrays.asList(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.floatwindow.FloatWindowListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.htc.pitroad",
                    "com.htc.pitroad.landingpage.activity.LandingPageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.entry.FunctionActivity"
                )
            ).setData(Uri.parse("mobilemanager://function/entry/AutoStart"))
        )

        public fun isCallable(
            context: Context,
            intent: Intent
        ): Boolean {
            val list =
                context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            return list.size > 0
        }
    }

    enum class PermissionStatus {
        SUCCESS, ERROR, REQUESTED
    }
}