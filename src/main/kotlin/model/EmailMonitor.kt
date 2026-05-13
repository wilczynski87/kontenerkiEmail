package com.kontenery.model

interface EmailMonitor {
    fun record(event: EmailSendEvent)
}