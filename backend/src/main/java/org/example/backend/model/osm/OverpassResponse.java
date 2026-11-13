package org.example.backend.model.osm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Antwort der Overpass API (OpenStreetMap). Nur die Felder, die wir brauchen.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OverpassResponse(List<Element> elements) {

    /**
     * Ein OSM-Objekt: "node" (Punkt) hat lat/lon direkt,
     * "way" (Gebäude-Umriss) hat stattdessen einen Mittelpunkt "center".
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Element(String type, long id, Double lat, Double lon, Center center, Map<String, String> tags) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Center(double lat, double lon) {
    }
}
