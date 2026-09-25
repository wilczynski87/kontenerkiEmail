package service

import com.kontenery.service.GmailHealthService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GmailHealthServiceTest {

    @Test
    fun `clientIdSuffix keeps short ids`() {
        assertEquals(
            "short-id",
            GmailHealthService.clientIdSuffix("short-id.apps.googleusercontent.com"),
        )
    }

    @Test
    fun `clientIdSuffix truncates long prefixes`() {
        val suffix = GmailHealthService.clientIdSuffix(
            "695782084929-ien1q7q762li8i172q4ecllrsh3ha3no.apps.googleusercontent.com"
        )
        assertTrue(suffix.startsWith("…"))
        assertTrue(suffix.length <= 13)
    }
}
