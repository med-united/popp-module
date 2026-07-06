package de.servicehealth.poppmodule.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class BrandSearchTextsTest {
    @Test
    fun forEntityGeneratesPlaceholderAndNoResultsText() {
        val texts = BrandSearchTexts.forEntity("Versicherung")
        assertEquals("Versicherung suchen…", texts.placeholder)
        assertEquals("Keine Versicherung gefunden.", texts.noResultsText)
    }

    @Test
    fun copyAllowsPlaceholderOverride() {
        val texts =
            BrandSearchTexts.forEntity("Einrichtung")
                .copy(placeholder = "Apotheke oder Praxis suchen…")
        assertEquals("Apotheke oder Praxis suchen…", texts.placeholder)
        assertEquals("Keine Einrichtung gefunden.", texts.noResultsText)
    }
}
