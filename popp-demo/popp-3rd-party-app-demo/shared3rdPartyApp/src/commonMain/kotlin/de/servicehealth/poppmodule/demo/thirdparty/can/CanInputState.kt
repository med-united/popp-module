package de.servicehealth.poppmodule.demo.thirdparty.can

/** Number of digits in an eGK Card Access Number. */
const val CAN_LENGTH = 6

/**
 * Immutable state of the CAN entry. [digits] holds 0..[CAN_LENGTH] numeric characters.
 * All transitions are pure so they can be unit-tested without Compose.
 */
data class CanInputState(val digits: String = "") {
    val isComplete: Boolean get() = digits.length == CAN_LENGTH

    fun appendDigit(digit: Char): CanInputState =
        if (digits.length < CAN_LENGTH && digit.isDigit()) copy(digits = digits + digit) else this

    fun backspace(): CanInputState =
        if (digits.isEmpty()) this else copy(digits = digits.dropLast(1))

    fun cleared(): CanInputState = CanInputState()
}

/** True when [value] is a syntactically valid CAN (exactly [CAN_LENGTH] digits). */
fun isValidCan(value: String): Boolean =
    value.length == CAN_LENGTH && value.all { it.isDigit() }
