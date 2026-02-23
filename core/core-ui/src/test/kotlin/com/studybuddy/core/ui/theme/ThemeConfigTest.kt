package com.studybuddy.core.ui.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ThemeConfigTest {

    @Test
    fun `fromId sunset returns Sunset`() {
        assertEquals(ThemeConfig.Sunset, ThemeConfig.fromId("sunset"))
    }

    @Test
    fun `fromId ocean returns Ocean`() {
        assertEquals(ThemeConfig.Ocean, ThemeConfig.fromId("ocean"))
    }

    @Test
    fun `fromId unknown falls back to Sunset`() {
        assertEquals(ThemeConfig.Sunset, ThemeConfig.fromId("unknown"))
    }

    @Test
    fun `fromId empty string falls back to Sunset`() {
        assertEquals(ThemeConfig.Sunset, ThemeConfig.fromId(""))
    }

    @Test
    fun `fromId is case insensitive`() {
        assertEquals(ThemeConfig.Sunset, ThemeConfig.fromId("SUNSET"))
    }

    @Test
    fun `fromId mixed case ocean returns Ocean`() {
        assertEquals(ThemeConfig.Ocean, ThemeConfig.fromId("OcEaN"))
    }

    @Test
    fun `fromId forest returns Forest`() {
        assertEquals(ThemeConfig.Forest, ThemeConfig.fromId("forest"))
    }

    @Test
    fun `fromId galaxy returns Galaxy`() {
        assertEquals(ThemeConfig.Galaxy, ThemeConfig.fromId("galaxy"))
    }

    @Test
    fun `fromId candy returns Candy`() {
        assertEquals(ThemeConfig.Candy, ThemeConfig.fromId("candy"))
    }

    @Test
    fun `fromId arctic returns Arctic`() {
        assertEquals(ThemeConfig.Arctic, ThemeConfig.fromId("arctic"))
    }
}
