package com.ibracero.retrum.common

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visibleOrGone(visible: Boolean) {
    if (visible) visible() else gone()
}

fun View.visibleOrInvisible(visible: Boolean) {
    if (visible) visible() else invisible()
}

fun ViewGroup.inflate(layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)

@ColorInt
fun View.getColor(@ColorRes color: Int) = ContextCompat.getColor(this.context, color)

fun View?.showKeyboard() {
    if (this == null) return
    val focusRequested = requestFocus()
    if (!focusRequested) {
        Timber.d("Focus requested but not succeeded.")
        return
    }
    var imm: InputMethodManager? = null
    try {
        imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    } catch (illegalStateException: IllegalStateException) {
        Timber.d(illegalStateException)
    }

    imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun View?.hideKeyboard() {
    if (this == null) return
    requestFocus()

    var imm: InputMethodManager? = null
    try {
        imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    } catch (illegalStateException: IllegalStateException) {
        Timber.d(illegalStateException)
    }

    imm?.hideSoftInputFromWindow(windowToken, 0);
}