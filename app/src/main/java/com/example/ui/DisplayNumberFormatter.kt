package com.example.ui

/** Converts Arabic-Indic and Eastern Arabic-Indic digits in display strings only. */
object DisplayNumberFormatter {
    fun westernDigits(value: String): String = buildString(value.length) {
        value.forEach { char ->
            append(
                when (char) {
                    in '٠'..'٩' -> ('0'.code + (char.code - '٠'.code)).toChar()
                    in '۰'..'۹' -> ('0'.code + (char.code - '۰'.code)).toChar()
                    else -> char
                }
            )
        }
    }
}
