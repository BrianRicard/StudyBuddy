package com.studybuddy.core.ui.theme

enum class ThemeConfig(val displayName: String) {
    Sunset("Sunset"),
    Ocean("Ocean"),
    Forest("Forest"),
    Galaxy("Galaxy"),
    Candy("Candy"),
    Arctic("Arctic");

    companion object {
        fun fromId(id: String): ThemeConfig =
            entries.firstOrNull { it.name.lowercase() == id.lowercase() } ?: Sunset
    }
}
