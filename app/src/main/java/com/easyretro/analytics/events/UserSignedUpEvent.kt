package com.easyretro.analytics.events

import com.easyretro.analytics.AnalyticsEvent
import com.easyretro.analytics.EventType
import java.util.*

object UserSignedUpEvent : AnalyticsEvent {
    override val type: String
        get() = EventType.USER_SIGNED_UP.toString().lowercase(Locale.getDefault())

    override val properties: Map<String, String>? = null
}