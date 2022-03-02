package com.easyretro.analytics.events

import com.easyretro.analytics.AnalyticsEvent
import com.easyretro.analytics.EventType
import java.util.*

object UserGoogleSignedInEvent : AnalyticsEvent {
    override val type: String
        get() = EventType.USER_GOOGLE_SIGN_IN.toString().lowercase(Locale.getDefault())

    override val properties: Map<String, String>? = null
}