package com.kontenery

import com.kontenery.data.invoice.Invoice
import com.kontenery.model.ConfigApp
import com.kontenery.oauth.createAutoRefreshTokenProvider
import com.kontenery.service.DocumentService
import com.kontenery.service.GmailRestService
import com.kontenery.service.MailService
import com.kontenery.service.SendRequest
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val configApp = ConfigApp.load()
    println("\nŚrodowisko: ${configApp.env}\n")

    val internalClient = createHttpClient()
    val gmailClient = createHttpClient()
    val oauthClient = createHttpClient()

    val tokenProvider = createAutoRefreshTokenProvider(configApp, oauthClient)
    val tokenRefreshScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    tokenProvider.startBackgroundRefresh(tokenRefreshScope)

    environment.monitor.subscribe(ApplicationStopped) {
        tokenRefreshScope.cancel()
    }

    val gmailService = GmailRestService(gmailClient, tokenProvider)
    val documentService = DocumentService()
    val sendRequest = SendRequest(internalClient, configApp)
    val mailService = MailService(
        gmailService,
        sendRequest,
        documentService,
        configApp
    )

    val mailQueue: Channel<Invoice> = Channel(capacity = Channel.UNLIMITED)

    configureSerialization()
    configureRouting(mailQueue, mailService)
    sendingMails(mailQueue, mailService)
}
