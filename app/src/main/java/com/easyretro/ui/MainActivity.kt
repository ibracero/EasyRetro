package com.easyretro.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.easyretro.R
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
    }

    override fun onStart() {
        super.onStart()
        handleDeepLink()
    }

    private fun handleDeepLink() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener {
                Timber.d("Dynamic link received ${it?.link ?: ""}")
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }
}