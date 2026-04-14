package com.example.callstresstest

internal object CallNumberUtils {
    private val separatorRegex = Regex("[\\s\\-()]")

    fun formatForDialing(rawInput: String): String? {
        var cleaned = rawInput.trim().replace(separatorRegex, "")
        if (cleaned.isEmpty()) {
            return null
        }

        if (cleaned.startsWith("00")) {
            cleaned = "+${cleaned.drop(2)}"
        }

        return if (cleaned.startsWith("+")) {
            formatInternational(cleaned)
        } else {
            formatDigitsOnly(cleaned)
        }
    }

    fun numbersMatch(first: String?, second: String?): Boolean {
        if (first.isNullOrBlank() || second.isNullOrBlank()) {
            return false
        }

        val normalizedFirst = digitsOnly(first)
        val normalizedSecond = digitsOnly(second)
        if (normalizedFirst.isEmpty() || normalizedSecond.isEmpty()) {
            return false
        }

        if (normalizedFirst == normalizedSecond) {
            return true
        }

        if (normalizedFirst.length >= 11 && normalizedSecond.length >= 11 &&
            normalizedFirst.takeLast(11) == normalizedSecond.takeLast(11)
        ) {
            return true
        }

        return normalizedFirst.length >= 7 &&
            normalizedSecond.length >= 7 &&
            normalizedFirst.takeLast(7) == normalizedSecond.takeLast(7)
    }

    fun digitsOnly(value: String): String = value.filter(Char::isDigit)

    private fun formatInternational(value: String): String? {
        val digits = value.drop(1).filter(Char::isDigit)
        return if (digits.length in 7..15) {
            "+$digits"
        } else {
            null
        }
    }

    private fun formatDigitsOnly(value: String): String? {
        return when {
            value.length == 11 && value.startsWith("1") -> "+86$value"
            value.startsWith("86") && value.length in 12..15 -> "+$value"
            else -> null
        }
    }
}
