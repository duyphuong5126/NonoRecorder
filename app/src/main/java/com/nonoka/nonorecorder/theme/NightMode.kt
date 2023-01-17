package com.nonoka.nonorecorder.theme

enum class NightMode(val id: Int) {
    Light(1001), Dark(2002), System(3003);

    companion object {
        @JvmStatic
        fun fromId(id: Int): NightMode {
            return values().firstOrNull {
                it.id == id
            } ?: throw IllegalArgumentException("Invalid ID $id")
        }
    }
}