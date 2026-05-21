package com.kontenery.service

import com.google.api.services.gmail.model.Message
import com.kontenery.oauth.AutoRefreshTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.Base64
import kotlin.collections.mapOf

class GmailRestService(
    private val httpClient: HttpClient,
    private val tokenProvider: AutoRefreshTokenProvider
) {
    suspend fun send(message: Message) {
        val token = tokenProvider.getAccessToken()

        httpClient.post(
            "https://gmail.googleapis.com/gmail/v1/users/me/messages/send"
        ) {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(message)
        }
    }
}
