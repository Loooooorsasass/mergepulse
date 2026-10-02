package com.example.game.model

enum class Charge {
    POSITIVE,
    NEGATIVE;

    fun flipped(): Charge {
        return if (this == POSITIVE) NEGATIVE else POSITIVE
    }

    val symbol: String
        get() = if (this == POSITIVE) "+" else "-"
}
