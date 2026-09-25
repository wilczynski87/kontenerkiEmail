package com.kontenery.service

import com.google.api.services.gmail.model.Message
import com.kontenery.oauth.AutoRefreshTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GmailRestService(
    private val httpClient: HttpClient,
    private val tokenProvider: AutoRefreshTokenProvider,
) {
    suspend fun send(message: Message) {
        val token = tokenProvider.getAccessToken()

        val response = httpClient.post(
            "https://gmail.googleapis.com/gmail/v1/users/me/messages/send"
        ) {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(message)
        }

        if (!response.status.isSuccess()) {
            error("Gmail send failed: ${response.status} ${response.bodyAsText()}")
        }
    }

    /**
     * Validates access token with Google and returns granted scopes.
     * Works with gmail.send-only tokens (unlike users/me/profile).
     */
    suspend fun fetchTokenInfo(accessToken: String): GoogleTokenInfo {
        val response = httpClient.get("https://oauth2.googleapis.com/tokeninfo") {
            parameter("access_token", accessToken)
        }

        if (!response.status.isSuccess()) {
            error("Token info failed: ${response.status} ${response.bodyAsText()}")
        }

        return response.body()
    }
}

@Serializable
data class GoogleTokenInfo(
    @SerialName("azp") val authorizedParty: String? = null,
    @SerialName("aud") val audience: String? = null,
    @SerialName("scope") val scope: String? = null,
    @SerialName("exp") val expiresAtEpochSeconds: String? = null,
    @SerialName("expires_in") val expiresInSeconds: String? = null,
    @SerialName("email") val email: String? = null,
) {
    fun scopes(): Set<String> =
        scope?.split(' ')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet().orEmpty()

    fun hasGmailSendCapability(): Boolean {
        val scopes = scopes()
        return GMAIL_SEND_SCOPES.any { it in scopes }
    }

    companion object {
        val GMAIL_SEND_SCOPES = setOf(
            "https://www.googleapis.com/auth/gmail.send",
            "https://mail.google.com/",
            "https://www.googleapis.com/auth/gmail.modify",
            "https://www.googleapis.com/auth/gmail.compose",
        )
    }
}
