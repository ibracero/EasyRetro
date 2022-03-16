package com.easyretro.common.extensions

import android.content.Context
import android.content.res.Configuration
import kotlin.math.min

fun Context?.isPortraitMode(): Boolean {
    val orientation = this?.resources?.configuration?.orientation ?: Configuration.ORIENTATION_PORTRAIT
    return orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Context?.isTablet(): Boolean {
    val metrics = this?.resources?.displayMetrics ?: return false
    val widthDp = metrics.widthPixels / metrics.density
    val heightDp = metrics.heightPixels / metrics.density
    return min(widthDp, heightDp) >= 720
}