package com.easyretro.common.extensions

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.gone() {
    visibility = View.GONE
}

fun View.isGone() = visibility == View.GONE

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
        Timber.e(illegalStateException)
    }

    imm?.hideSoftInputFromWindow(windowToken, 0);
}

fun EditText.addTextWatcher(
    beforeTextChanged: (() -> Unit)? = null,
    onTextChanged: (() -> Unit)? = null,
    afterTextChanged: (() -> Unit)? = null
) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged?.invoke()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeTextChanged?.invoke()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged?.invoke()
        }

    })
}

fun TextInputLayout.hasValidText() =
    this.error == null && this.editText?.text?.isEmpty() == false
