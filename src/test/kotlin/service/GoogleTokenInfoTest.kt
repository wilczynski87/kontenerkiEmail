package service

import com.kontenery.service.GoogleTokenInfo
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoogleTokenInfoTest {

    @Test
    fun `detects gmail send scope`() {
        val info = GoogleTokenInfo(
            scope = "https://www.googleapis.com/auth/gmail.send openid"
        )
        assertTrue(info.hasGmailSendCapability())
    }

    @Test
    fun `detects full mail scope`() {
        val info = GoogleTokenInfo(scope = "https://mail.google.com/")
        assertTrue(info.hasGmailSendCapability())
    }

    @Test
    fun `rejects missing send scope`() {
        val info = GoogleTokenInfo(scope = "openid email profile")
        assertFalse(info.hasGmailSendCapability())
    }
}
