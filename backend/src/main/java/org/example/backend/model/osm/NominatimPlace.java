package org.example.backend.model.osm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Ein Restaurant-Treffer der Nominatim-Suche (Ausweichquelle, wenn Overpass nicht antwortet).
 * extratags enthält u. a. cuisine, opening_hours, website; address enthält road, house_number, city.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimPlace(@JsonProperty("osm_type") String osmType,
                             @JsonProperty("osm_id") long osmId,
                             String name,
                             String lat,
                             String lon,
                             Map<String, String> extratags,
                             Map<String, String> address) {
}
