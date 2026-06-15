package de.servicehealth.poppmodule.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class BrandColorsTest {
    @Test
    fun defaultPalette_hasBrandSignalColors() {
        val colors = BrandColors()
        assertEquals(Color(0xFF5F29EF), colors.violet, "Electric Violet")
        assertEquals(Color(0xFFF7EA5A), colors.yellow, "Signal Yellow")
        assertEquals(Color(0xFF100030), colors.deep, "Deep Space")
    }
}
