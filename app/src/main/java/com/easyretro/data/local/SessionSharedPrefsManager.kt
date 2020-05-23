package com.easyretro.data.local

import android.content.Context

class SessionSharedPrefsManager(context: Context) : SessionManager {

    companion object {
        private const val SESSION_SHARED_PREFERENCES = "SESSION_SHARED_PREFERENCES"
        private const val PARAM_SESSION = "PARAM_SESSION"
    }

    private val sharedPreferences =
        context.getSharedPreferences(SESSION_SHARED_PREFERENCES, Context.MODE_PRIVATE)

    override fun setSessionStarted() =
        sharedPreferences
            .edit()
            .putBoolean(PARAM_SESSION, true)
            .apply()

    override fun setSessionEnded() =
        sharedPreferences.edit()
            .clear()
            .apply()

    override fun isSessionStarted(): Boolean = sharedPreferences.getBoolean(PARAM_SESSION, false)
}