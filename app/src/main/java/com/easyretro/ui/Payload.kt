package com.easyretro.ui

sealed class Payload {
    data class CreateRetroPayload(val success: Boolean) : Payload()
    data class CreateStatementPayload(val success: Boolean) : Payload()
    data class DescriptionPayload(val description: String) : Payload()
}