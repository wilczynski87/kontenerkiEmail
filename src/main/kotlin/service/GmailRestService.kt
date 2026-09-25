package com.kontenery.service

import com.google.api.services.gmail.model.Message
import com.kontenery.oauth.AutoRefreshTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
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
     * Verifies the access token can call Gmail API (read profile).
     * Confirms connectivity and that credentials grant Gmail access.
     */
    suspend fun fetchProfile(accessToken: String): GmailProfile {
        val response = httpClient.get(
            "https://gmail.googleapis.com/gmail/v1/users/me/profile"
        ) {
            header("Authorization", "Bearer $accessToken")
        }

        if (!response.status.isSuccess()) {
            error("Gmail profile failed: ${response.status} ${response.bodyAsText()}")
        }

        return response.body()
    }
}

@Serializable
data class GmailProfile(
    @SerialName("emailAddress") val emailAddress: String? = null,
    @SerialName("messagesTotal") val messagesTotal: Long? = null,
    @SerialName("threadsTotal") val threadsTotal: Long? = null,
    @SerialName("historyId") val historyId: String? = null,
)
