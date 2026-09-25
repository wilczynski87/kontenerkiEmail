package model

import com.kontenery.model.ConfigApp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConfigAppGoogleClientIdTest {

    @Test
    fun `accepts single client id`() {
        val id = "123456789-abc.apps.googleusercontent.com"
        assertEquals(id, ConfigApp.requireSingleGoogleClientId(id))
    }

    @Test
    fun `rejects comma-separated client ids`() {
        assertFailsWith<IllegalArgumentException> {
            ConfigApp.requireSingleGoogleClientId(
                "aaa.apps.googleusercontent.com,bbb.apps.googleusercontent.com"
            )
        }
    }
}
