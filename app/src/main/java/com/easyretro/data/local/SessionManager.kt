package com.easyretro.data.local

interface SessionManager {

    fun setSessionStarted()

    fun setSessionEnded()

    fun isSessionStarted(): Boolean
}