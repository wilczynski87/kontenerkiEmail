package oauth

import com.kontenery.oauth.FileRefreshTokenStore
import kotlin.test.Test
import kotlin.test.assertEquals
import java.nio.file.Files

class FileRefreshTokenStoreTest {

    @Test
    fun `saves and loads refresh token from file`() {
        val dir = Files.createTempDirectory("oauth-test")
        val tokenFile = dir.resolve("refresh.token")

        val store = FileRefreshTokenStore(tokenFile, "initial-token")
        assertEquals("initial-token", store.getRefreshToken())

        store.saveRefreshToken("rotated-token")
        assertEquals("rotated-token", store.getRefreshToken())

        val reloaded = FileRefreshTokenStore(tokenFile, "ignored-on-load")
        assertEquals("rotated-token", reloaded.getRefreshToken())
    }
}
