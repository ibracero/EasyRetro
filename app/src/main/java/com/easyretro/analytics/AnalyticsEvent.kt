package com.easyretro.analytics

interface AnalyticsEvent {
    val type: String
    val properties: Map<String, String>
}

enum class EventType {
    PAGE_ENTER,
    TAP
}