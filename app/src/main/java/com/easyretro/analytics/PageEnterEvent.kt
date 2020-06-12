package com.easyretro.analytics

import java.util.*

data class PageEnterEvent(private val screen: Screen) : AnalyticsEvent {

    override val type: String
        get() = EventType.PAGE_ENTER.toString().toLowerCase(Locale.getDefault())

    override val properties: Map<String, String>
        get() = mapOf("screen" to screen.name.toLowerCase(Locale.getDefault()))

}