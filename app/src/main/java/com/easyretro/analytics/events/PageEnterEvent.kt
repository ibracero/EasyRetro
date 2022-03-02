package com.easyretro.analytics.events

import com.easyretro.analytics.AnalyticsEvent
import com.easyretro.analytics.EventType
import com.easyretro.analytics.Screen
import java.util.*

data class PageEnterEvent(private val screen: Screen) :
    AnalyticsEvent {

    override val type: String
        get() = EventType.PAGE_ENTER.toString().lowercase(Locale.getDefault())

    override val properties: Map<String, String>
        get() = mapOf("screen" to screen.name.lowercase(Locale.getDefault()))

}