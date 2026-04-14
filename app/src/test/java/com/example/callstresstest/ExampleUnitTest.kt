package com.example.callstresstest

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun formatForDialing_formatsMainlandChinaNumber() {
        assertEquals("+8613800138000", CallNumberUtils.formatForDialing("13800138000"))
    }

    @Test
    fun formatForDialing_acceptsExplicitInternationalNumber() {
        assertEquals("+14155552671", CallNumberUtils.formatForDialing("+1 415 555 2671"))
    }

    @Test
    fun formatForDialing_rejectsAmbiguousInternationalDigits() {
        assertNull(CallNumberUtils.formatForDialing("4155552671"))
    }

    @Test
    fun numbersMatch_matchesCountryCodeVariants() {
        assertTrue(CallNumberUtils.numbersMatch("+8613800138000", "13800138000"))
    }

    @Test
    fun numbersMatch_rejectsDifferentNumbers() {
        assertFalse(CallNumberUtils.numbersMatch("+8613800138000", "+8613800138999"))
    }
}
