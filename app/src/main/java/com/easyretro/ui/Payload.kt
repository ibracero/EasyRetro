package com.easyretro.ui

sealed class Payload {
    data class CreateRetroPayload(val success: Boolean) : Payload()
    data class CreateStatementPayload(val success: Boolean) : Payload()
    data class StatementContentPayload(val description: String, val isRemovable: Boolean) : Payload()
    data class RetroLockPayload(val retroLocked: Boolean): Payload()
}