package com.kontenery.model

import io.ktor.client.request.forms.submitForm
import io.ktor.client.HttpClient
import io.ktor.http.*
import io.ktor.client.call.*
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GoogleTokenProvider(
    private val httpClient: HttpClient,
    private val configApp: ConfigApp,
) {
    @Volatile
    private var cachedToken: String? = null
    @Volatile
    private var expiry: Long = 0

    suspend fun getAccessToken(): String {
        val now = System.currentTimeMillis()

        if (cachedToken != null && now < expiry - 60_000) {
            return cachedToken!!
        }

        return try {
            val response = httpClient.submitForm(
                url = "https://oauth2.googleapis.com/token",
                formParameters = Parameters.build {
                    append("client_id", configApp.clientId)
                    append("client_secret", configApp.clientSecret)
                    append("refresh_token", configApp.refreshToken)
                    append("grant_type", "refresh_token")
                }
            )

            if (response.status.isSuccess()) {
                val token = response.body<GoogleTokenResponse>()
                println("Nowy access token: ${token.accessToken}")
                cachedToken = token.accessToken
                expiry = now + (token.expiresIn * 1000L) - 30_000
                cachedToken!!
            } else {
                val errorBody = response.bodyAsText()
                println("Błąd Google API: $errorBody")
                error("błąd przy autoryzacji: $errorBody")
            }
        } catch (e: Exception) {
            println("Błąd sieci/serializacji: ${e.message}")
            error("błąd przy autoryzacji: ${e.message}")
        }
    }
}

@Serializable
data class GoogleTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("scope") val scope: String,
    @SerialName("token_type") val tokenType: String,
    // Uwaga: Google nie zawsze zwraca nowy refresh_token przy odświeżaniu
    @SerialName("refresh_token") val refreshToken: String? = null
)
