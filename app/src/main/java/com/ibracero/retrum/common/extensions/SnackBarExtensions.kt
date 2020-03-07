package com.ibracero.retrum.common.extensions

import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ibracero.retrum.R

fun View.showSuccessSnackbar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
    return showSnackBar(message, duration, R.color.successColor)
}

fun View.showErrorSnackbar(
    @StringRes message: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @StringRes actionText: Int? = null,
    action: (() -> Unit)? = null
): Snackbar {
    return showSnackBar(message, duration, R.color.errorColor, actionText, action)
}

fun View.showSnackBar(
    @StringRes message: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @ColorRes colorRes: Int,
    @StringRes actionText: Int? = null,
    action: (() -> Unit)? = null
): Snackbar {
    val snackBar = Snackbar.make(this, message, duration)
    if (actionText != null && action != null) {
        snackBar.setAction(actionText) { action() }
        snackBar.setActionStyle()
    }
    if (colorRes != 0) {
        val color = ContextCompat.getColor(this.context, colorRes)
        snackBar.view.setBackgroundColor(color)
    }
    val snackBarTextView: TextView = snackBar.view.findViewById(com.google.android.material.R.id.snackbar_text)
    snackBarTextView.maxLines = 2
    snackBar.show()
    return snackBar
}

fun Snackbar.setActionStyle(): Snackbar {
    val snackbarActionTextView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
    snackbarActionTextView.setTypeface(snackbarActionTextView.typeface, Typeface.BOLD)
    return this
}
