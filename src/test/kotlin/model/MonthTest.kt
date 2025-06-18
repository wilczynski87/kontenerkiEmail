package model

import com.kontenery.library.utils.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class MonthTest {

        @Test
        fun fromNumber_returnsCorrectMonth() {
                assertEquals(Month.JANUARY, Month.fromNumber(1))
                assertEquals(Month.DECEMBER, Month.fromNumber(12))
                assertEquals(Month.JUNE, Month.fromNumber(6))
        }

        @Test
        fun fromNumber_returnsNullForInvalidMonth() {
                assertNull(Month.fromNumber(0))
                assertNull(Month.fromNumber(13))
        }

        @Test
        fun fromString_returnsCorrectMonth() {
                assertEquals(Month.JANUARY, Month.fromString("2025-01-01"))
                assertEquals(Month.MAY, Month.fromString("2025-05-15"))
                assertEquals(Month.DECEMBER, Month.fromString("2025-12-31"))
        }

        @Test
        fun fromString_throwsForInvalidFormat() {
                assertFailsWith<IllegalArgumentException> {
                        Month.fromString("bad-format")
                }
        }

        @Test
        fun polishName_isCorrect() {
                assertEquals("Styczeń", Month.JANUARY.polishName)
                assertEquals("Grudzień", Month.DECEMBER.polishName)
        }
}