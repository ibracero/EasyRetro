package com.easyretro.analytics

import com.google.firebase.analytics.FirebaseAnalytics

fun reportAnalytics(event: AnalyticsEvent) {
    AnalyticsManager.logEvent(event)
}
