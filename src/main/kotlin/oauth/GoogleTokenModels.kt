package com.kontenery.oauth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("scope") val scope: String? = null,
    @SerialName("token_type") val tokenType: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
)

@Serializable
data class GoogleOAuthError(
    @SerialName("error") val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
)

data class RefreshedAccessToken(
    val accessToken: String,
    val expiresInSeconds: Int,
    val refreshToken: String? = null,
)
