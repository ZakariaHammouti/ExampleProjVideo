package org.example.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CuisineTranslatorTest {

    @Test
    void translate_knownValues() {
        assertEquals("Italienisch, Pizza", CuisineTranslator.translate("italian;pizza"));
    }

    @Test
    void translate_unknownValue_isCapitalized() {
        assertEquals("Fish and chips", CuisineTranslator.translate("fish_and_chips"));
    }

    @Test
    void translate_null_returnsNull() {
        assertNull(CuisineTranslator.translate(null));
    }
}
