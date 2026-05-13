package com.kontenery.model

import kotlinx.serialization.Serializable

@Serializable
data class EmailSendEvent(
    val invoiceNumber: String?,
    val to: String,
    val subject: String,
    val success: Boolean,
    val error: String? = null,
    val attempt: Int,
    val timestamp: Long = System.currentTimeMillis()
)

