package com.easyretro.analytics

import java.util.*

data class TapEvent(private val screen: Screen, private val uiValue: UiValue) : AnalyticsEvent {

    override val type: String
        get() = EventType.TAP.toString().toLowerCase(Locale.getDefault())

    override val properties: Map<String, String>
        get() = mapOf(
            "screen" to screen.name.toLowerCase(Locale.getDefault()),
            "ui_value" to uiValue.name.toLowerCase(Locale.getDefault())
        )
}