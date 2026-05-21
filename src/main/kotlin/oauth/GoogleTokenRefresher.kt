package com.kontenery.oauth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class GoogleTokenRefresher(
    private val httpClient: HttpClient,
    private val credentials: GoogleOAuthCredentials,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun refresh(refreshToken: String): RefreshedAccessToken {
        val response = httpClient.submitForm(
            url = TOKEN_URL,
            formParameters = Parameters.build {
                append("client_id", credentials.clientId)
                append("client_secret", credentials.clientSecret)
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
            }
        )

        if (response.status.isSuccess()) {
            val token = response.body<GoogleTokenResponse>()
            return RefreshedAccessToken(
                accessToken = token.accessToken,
                expiresInSeconds = token.expiresIn,
                refreshToken = token.refreshToken,
            )
        }

        val errorBody = response.bodyAsText()
        val googleError = runCatching { json.decodeFromString<GoogleOAuthError>(errorBody) }.getOrNull()

        val description = when (googleError?.error) {
            "invalid_grant" ->
                "Refresh token wygasł lub został unieważniony. Wygeneruj nowy GOOGLE_REFRESH_TOKEN i zapisz go w pliku lub zmiennej środowiskowej."
            else -> googleError?.errorDescription ?: errorBody
        }

        error("Błąd autoryzacji Gmail ($description)")
    }

    companion object {
        const val TOKEN_URL = "https://oauth2.googleapis.com/token"
    }
}
