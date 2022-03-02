package com.easyretro.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber
import java.util.*

object AnalyticsManager {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context, enableAnalytics: Boolean) {
        if (!enableAnalytics) return

        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun logEvent(event: AnalyticsEvent) {
        Timber.d("Event logged: ${event.type} ==> ${event.properties}")
        firebaseAnalytics?.logEvent(event.type.lowercase(Locale.getDefault()), event.properties.asBundle())
    }

    private fun Map<String, String>?.asBundle(): Bundle? {
        if (this == null) return null

        val bundle = Bundle()
        this.entries.forEach {
            bundle.putString(it.key, it.value)
        }
        return bundle
    }
}