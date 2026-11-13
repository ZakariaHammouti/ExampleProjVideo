package org.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Antwort von GET /api/places/nearby: Suchpunkt + gefundene Restaurants.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyResponse {
    private double latitude;
    private double longitude;
    private String locationName;  // z. B. "Köln, Nordrhein-Westfalen, Deutschland" oder "Dein Standort"
    private String provider;      // "Overpass" (vollständig) oder "Nominatim" (Ausweichquelle, max. 40 Treffer)
    private double radiusKm;
    private List<Place> places;
}
