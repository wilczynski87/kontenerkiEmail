package com.kontenery

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.model.ConfigApp
import com.kontenery.model.GoogleTokenProvider
import com.kontenery.service.DocumentService
import com.kontenery.service.GmailRestService
import com.kontenery.service.MailService
import com.kontenery.service.SendRequest
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
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

    val tokenProvider = GoogleTokenProvider(oauthClient, configApp)
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
