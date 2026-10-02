package com.example.game.engine

interface GameTextProvider {
    fun overchargeShockwave(): String
    fun scoreOverdrive(durationSeconds: Int): String
    fun graceRescue(points: Int): String
    fun comboSuffix(combo: Int): String
    fun overdriveTag(): String
    fun floatingScore(points: Int, combo: Int, overdrive: Boolean): String
}
