package com.kontenery.service

import com.kontenery.model.ConfigApp
import com.kontenery.oauth.AutoRefreshTokenProvider
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class GmailHealthService(
    private val tokenProvider: AutoRefreshTokenProvider,
    private val gmail: GmailRestService,
    private val config: ConfigApp,
) {
    private val lastResult = AtomicReference(
        GmailHealthResult(
            status = GmailHealthStatus.UNKNOWN,
            checkedAt = null,
            oauthOk = false,
            gmailApiOk = false,
            sendCapabilityOk = false,
            emailAddress = null,
            clientIdSuffix = clientIdSuffix(config.clientId),
            message = "Gmail health not checked yet",
        )
    )

    fun lastStatus(): GmailHealthResult = lastResult.get()

    /**
     * Live check: refresh OAuth token (validates client id/secret + refresh token),
     * then call Gmail profile API (validates connectivity + Gmail access / send capability).
     */
    suspend fun checkNow(): GmailHealthResult {
        val checkedAt = Instant.now().toString()

        return try {
            val accessToken = tokenProvider.forceRefreshAccessToken()
            val profile = gmail.fetchProfile(accessToken)
            val email = profile.emailAddress

            val emailMatchesConfig = email == null ||
                email.equals(config.emailUser, ignoreCase = true)

            val result = GmailHealthResult(
                status = if (emailMatchesConfig) GmailHealthStatus.UP else GmailHealthStatus.DEGRADED,
                checkedAt = checkedAt,
                oauthOk = true,
                gmailApiOk = true,
                sendCapabilityOk = true,
                emailAddress = email,
                clientIdSuffix = clientIdSuffix(config.clientId),
                message = when {
                    !emailMatchesConfig ->
                        "OAuth OK, but Gmail account ($email) differs from EMAIL_USER (${config.emailUser})"
                    else ->
                        "OAuth credentials valid; Gmail API reachable for $email"
                },
            )
            lastResult.set(result)
            result
        } catch (e: Exception) {
            val result = GmailHealthResult(
                status = GmailHealthStatus.DOWN,
                checkedAt = checkedAt,
                oauthOk = false,
                gmailApiOk = false,
                sendCapabilityOk = false,
                emailAddress = null,
                clientIdSuffix = clientIdSuffix(config.clientId),
                message = e.message ?: e.toString(),
            )
            lastResult.set(result)
            result
        }
    }

    companion object {
        fun clientIdSuffix(clientId: String): String {
            val prefix = clientId.substringBefore('.')
            return if (prefix.length <= 12) prefix else "…${prefix.takeLast(12)}"
        }
    }
}

@Serializable
enum class GmailHealthStatus {
    UNKNOWN,
    UP,
    DEGRADED,
    DOWN,
}

@Serializable
data class GmailHealthResult(
    val status: GmailHealthStatus,
    val checkedAt: String?,
    val oauthOk: Boolean,
    val gmailApiOk: Boolean,
    val sendCapabilityOk: Boolean,
    val emailAddress: String?,
    val clientIdSuffix: String,
    val message: String,
)

@Serializable
data class AppHealthResponse(
    val status: String,
    val gmail: GmailHealthResult,
)
