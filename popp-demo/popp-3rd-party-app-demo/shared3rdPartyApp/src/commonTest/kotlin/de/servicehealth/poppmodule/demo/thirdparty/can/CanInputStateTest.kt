package de.servicehealth.poppmodule.demo.thirdparty.can

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanInputStateTest {
    @Test fun appendsDigitsUpToSix() {
        var s = CanInputState()
        "123456".forEach { s = s.appendDigit(it) }
        assertEquals("123456", s.digits)
        assertTrue(s.isComplete)
    }

    @Test fun ignoresDigitsBeyondSix() {
        var s = CanInputState()
        "1234567".forEach { s = s.appendDigit(it) }
        assertEquals("123456", s.digits)
    }

    @Test fun ignoresNonDigits() {
        assertEquals("", CanInputState().appendDigit('a').digits)
    }

    @Test fun backspaceRemovesLastDigit() {
        assertEquals("12", CanInputState("123").backspace().digits)
    }

    @Test fun backspaceOnEmptyIsNoOp() {
        assertEquals("", CanInputState().backspace().digits)
    }

    @Test fun clearedResetsDigits() {
        assertEquals("", CanInputState("123456").cleared().digits)
    }

    @Test fun incompleteBelowSix() {
        assertFalse(CanInputState("12345").isComplete)
    }

    @Test fun isValidCanAcceptsSixDigits() {
        assertTrue(isValidCan("123456"))
    }

    @Test fun isValidCanRejectsWrongLength() {
        assertFalse(isValidCan("12345"))
        assertFalse(isValidCan("1234567"))
    }

    @Test fun isValidCanRejectsNonDigits() {
        assertFalse(isValidCan("12a456"))
    }

    @Test fun isValidCanRejectsEmpty() {
        assertFalse(isValidCan(""))
    }
}
