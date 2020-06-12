package com.easyretro.analytics

interface AnalyticsEvent {
    val type: String
    val properties: Map<String, String>?
}

enum class EventType {
    PAGE_ENTER,
    TAP,
    USER_SIGNED_IN,
    USER_SIGNED_UP,
    USER_GOOGLE_SIGN_IN,
    RETRO_CREATED,
    STATEMENT_CREATED
}