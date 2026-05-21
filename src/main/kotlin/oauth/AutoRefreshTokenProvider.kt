package com.kontenery.oauth

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AutoRefreshTokenProvider(
    private val credentials: GoogleOAuthCredentials,
    private val httpClient: HttpClient,
    private val tokenStore: RefreshTokenStore,
    private val refresher: GoogleTokenRefresher = GoogleTokenRefresher(httpClient, credentials),
) {
    private val mutex = Mutex()

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var expiryMs: Long = 0

    suspend fun getAccessToken(): String {
        val now = System.currentTimeMillis()
        cachedAccessToken?.let { token ->
            if (now < expiryMs) return token
        }
        return refreshAndCache()
    }

    fun startBackgroundRefresh(scope: CoroutineScope) {
        scope.launch {
            while (isActive) {
                val waitMs = timeUntilRefresh()
                delay(waitMs.coerceAtLeast(MIN_BACKGROUND_DELAY_MS))
                runCatching { refreshAndCache() }
                    .onFailure { e -> println("Auto refresh token w tle nie powiódł się: ${e.message}") }
            }
        }
    }

    private suspend fun refreshAndCache(): String = mutex.withLock {
        val now = System.currentTimeMillis()
        cachedAccessToken?.let { token ->
            if (now < expiryMs) return token
        }

        val refreshed = refresher.refresh(tokenStore.getRefreshToken())
        refreshed.refreshToken?.let { tokenStore.saveRefreshToken(it) }

        cachedAccessToken = refreshed.accessToken
        expiryMs = now + refreshed.expiresInSeconds * 1000L - EXPIRY_BUFFER_MS

        refreshed.accessToken
    }

    private fun timeUntilRefresh(): Long {
        val remaining = expiryMs - System.currentTimeMillis()
        return if (remaining > EXPIRY_BUFFER_MS) {
            remaining - EXPIRY_BUFFER_MS
        } else {
            DEFAULT_REFRESH_INTERVAL_MS
        }
    }

    companion object {
        private const val EXPIRY_BUFFER_MS = 60_000L
        private const val MIN_BACKGROUND_DELAY_MS = 30_000L
        private const val DEFAULT_REFRESH_INTERVAL_MS = 3_000_000L // ~50 min, gdy cache pusty
    }
}

fun createAutoRefreshTokenProvider(
    config: com.kontenery.model.ConfigApp,
    httpClient: HttpClient,
): AutoRefreshTokenProvider {
    val credentials = GoogleOAuthCredentials.from(config)
    val tokenStore = FileRefreshTokenStore(config.refreshTokenFile, config.refreshToken)
    return AutoRefreshTokenProvider(credentials, httpClient, tokenStore)
}
