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
            scopes = emptyList(),
            clientIdSuffix = clientIdSuffix(config.clientId),
            message = "Gmail health not checked yet",
        )
    )

    fun lastStatus(): GmailHealthResult = lastResult.get()

    /**
     * Live check:
     * 1) refresh OAuth (validates client id/secret + refresh token)
     * 2) tokeninfo (validates token + checks gmail.send / mail.google.com scopes)
     *
     * Does not call users/me/profile — that requires broader scopes than gmail.send.
     */
    suspend fun checkNow(): GmailHealthResult {
        val checkedAt = Instant.now().toString()
        val clientSuffix = clientIdSuffix(config.clientId)

        val accessToken = try {
            tokenProvider.forceRefreshAccessToken()
        } catch (e: Exception) {
            return store(
                GmailHealthResult(
                    status = GmailHealthStatus.DOWN,
                    checkedAt = checkedAt,
                    oauthOk = false,
                    gmailApiOk = false,
                    sendCapabilityOk = false,
                    emailAddress = null,
                    scopes = emptyList(),
                    clientIdSuffix = clientSuffix,
                    message = e.message ?: e.toString(),
                )
            )
        }

        val tokenInfo = try {
            gmail.fetchTokenInfo(accessToken)
        } catch (e: Exception) {
            return store(
                GmailHealthResult(
                    status = GmailHealthStatus.DOWN,
                    checkedAt = checkedAt,
                    oauthOk = true,
                    gmailApiOk = false,
                    sendCapabilityOk = false,
                    emailAddress = null,
                    scopes = emptyList(),
                    clientIdSuffix = clientSuffix,
                    message = "OAuth refresh OK, but tokeninfo failed: ${e.message}",
                )
            )
        }

        val scopes = tokenInfo.scopes().toList().sorted()
        val canSend = tokenInfo.hasGmailSendCapability()
        val email = tokenInfo.email ?: config.emailUser
        val audienceMatches = tokenInfo.audience.isNullOrBlank() ||
            tokenInfo.audience == config.clientId ||
            tokenInfo.authorizedParty == config.clientId

        val result = when {
            !canSend -> GmailHealthResult(
                status = GmailHealthStatus.DOWN,
                checkedAt = checkedAt,
                oauthOk = true,
                gmailApiOk = true,
                sendCapabilityOk = false,
                emailAddress = email,
                scopes = scopes,
                clientIdSuffix = clientSuffix,
                message = "OAuth OK, but token lacks gmail.send scope. Re-authorize with " +
                    "https://www.googleapis.com/auth/gmail.send (current: ${scopes.joinToString()})",
            )
            !audienceMatches -> GmailHealthResult(
                status = GmailHealthStatus.DEGRADED,
                checkedAt = checkedAt,
                oauthOk = true,
                gmailApiOk = true,
                sendCapabilityOk = true,
                emailAddress = email,
                scopes = scopes,
                clientIdSuffix = clientSuffix,
                message = "Token audience (${tokenInfo.audience}) differs from GOOGLE_CLIENT_ID",
            )
            else -> GmailHealthResult(
                status = GmailHealthStatus.UP,
                checkedAt = checkedAt,
                oauthOk = true,
                gmailApiOk = true,
                sendCapabilityOk = true,
                emailAddress = email,
                scopes = scopes,
                clientIdSuffix = clientSuffix,
                message = "OAuth credentials valid; Gmail send scope OK for $email",
            )
        }

        return store(result)
    }

    private fun store(result: GmailHealthResult): GmailHealthResult {
        lastResult.set(result)
        return result
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
    val scopes: List<String> = emptyList(),
    val clientIdSuffix: String,
    val message: String,
)

@Serializable
data class AppHealthResponse(
    val status: String,
    val gmail: GmailHealthResult,
)
