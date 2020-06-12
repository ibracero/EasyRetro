package com.easyretro.analytics.events

import com.easyretro.analytics.AnalyticsEvent
import com.easyretro.analytics.EventType
import java.util.*

object StatementCreatedEvent : AnalyticsEvent {
    override val type: String
        get() = EventType.STATEMENT_CREATED.toString().toLowerCase(Locale.getDefault())

    override val properties: Map<String, String>? = null
}