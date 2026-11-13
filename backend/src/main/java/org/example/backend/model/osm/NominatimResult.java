package org.example.backend.model.osm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ein Treffer der Nominatim-Suche (Stadtname -> Koordinaten).
 * Nominatim liefert lat/lon als Text, z. B. "50.938361".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimResult(String lat, String lon, @JsonProperty("display_name") String displayName) {
}
