package com.rohitthebest.passwordsaver.util

import java.util.regex.Pattern

class CheckPasswordPattern {

    companion object {
        private val PASSWORD_PATTERN: Pattern =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}\$")

        private val a_digit_pattern: Pattern =
            Pattern.compile(".*?[0-9].*?")

        private val a_letter_pattern: Pattern =
            Pattern.compile(".*?[a-zA-Z].*?")

        private val a_special_character_pattern: Pattern =
            Pattern.compile(".*?[@#\$%^&+=].*?")

        private val min_length_pattern: Pattern =
            Pattern.compile(".{8,}")

        private val a_small_letter_pattern: Pattern =
            Pattern.compile(".*?[a-z].*?")

        private val a_capital_letter_pattern: Pattern =
            Pattern.compile(".*?[A-Z].*?")

    }

    fun check_password(password: String): Boolean {

        return PASSWORD_PATTERN.matcher(password).matches()
    }

    fun check_for_digit(password: String): Boolean {

        return a_digit_pattern.matcher(password).matches()

    }

    fun check_for_letter(password: String): Boolean {

        return a_letter_pattern.matcher(password).matches()
    }

    fun check_for_special_character(password: String): Boolean {

        return a_special_character_pattern.matcher(password).matches()
    }

    fun check_for_min_length(password: String): Boolean {

        return min_length_pattern.matcher(password).matches()
    }

    fun check_for_a_small_letter(password: String): Boolean {

        return a_small_letter_pattern.matcher(password).matches()
    }

    fun check_for_a_capital_letter(password: String): Boolean {

        return a_capital_letter_pattern.matcher(password).matches()
    }

}