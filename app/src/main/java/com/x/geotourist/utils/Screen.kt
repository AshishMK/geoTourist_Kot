package com.x.geotourist.utils

import android.util.DisplayMetrics
import com.x.geotourist.application.AppController

object Screen {
    const val MATCH_PARENT = -1
    const val WRAP_CONTENT = -2
    private var density = 0f
    private var scaledDensity = 0f

    var displayMetrics = DisplayMetrics()

    @JvmStatic
    public fun dp(dp: Int): Int {
        if (density == 0f) density =
            AppController.getInstance().resources.displayMetrics.density
        displayMetrics = AppController.getInstance().resources.displayMetrics
        return (dp * density + .5f).toInt()
    }


    @JvmStatic
    fun sp(sp: Float): Int {
        if (scaledDensity == 0f) scaledDensity =
            AppController.getInstance().resources.displayMetrics.scaledDensity
        return (sp * scaledDensity + .5f).toInt()
    }

    @JvmStatic
    fun getWidth(): Int {
        return AppController.getInstance().resources.displayMetrics.widthPixels
    }

    @JvmStatic
    fun getHeight(): Int {
        return AppController.getInstance().resources.displayMetrics.heightPixels
    }


}