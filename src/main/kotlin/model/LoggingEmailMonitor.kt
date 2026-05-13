package com.kontenery.model

class LoggingEmailMonitor : EmailMonitor {

    private val failures = mutableListOf<EmailSendEvent>()

    override fun record(event: EmailSendEvent) {
        if (event.success) {
            println("📧 EMAIL OK -> ${event.to}")
        } else {
            println("❌ EMAIL FAIL -> ${event.to} | ${event.error}")
            failures.add(event)
        }
    }

    fun getFailures(): List<EmailSendEvent> = failures
}