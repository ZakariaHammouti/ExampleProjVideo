package org.example.backend.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    void distanceKm_koelnToDuesseldorf_isAbout34km() {
        double d = GeoUtils.distanceKm(50.9375, 6.9603, 51.2277, 6.7735);
        assertEquals(34.8, d, 0.5);
    }

    @Test
    void distanceKm_samePoint_isZero() {
        assertEquals(0.0, GeoUtils.distanceKm(50.0, 7.0, 50.0, 7.0), 1e-9);
    }

    @Test
    void isValid_checksRanges() {
        assertTrue(GeoUtils.isValid(50.9, 6.9));
        assertFalse(GeoUtils.isValid(91, 0));
        assertFalse(GeoUtils.isValid(0, 181));
    }
}
