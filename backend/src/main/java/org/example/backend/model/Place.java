package org.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ein Restaurant aus OpenStreetMap. Wird nicht gespeichert, sondern live abgefragt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    private String id;            // z. B. "osm-node-123456"
    private String name;
    private String cuisine;       // auf Deutsch übersetzt, kann null sein
    private String address;       // Straße + Hausnummer, kann null sein
    private String city;          // kann null sein
    private double latitude;
    private double longitude;
    private double distanceKm;    // Entfernung zum Suchpunkt
    private String openingHours;  // OSM-Format, z. B. "Mo-Sa 12:00-22:00", kann null sein
    private String website;       // kann null sein
    @Builder.Default
    private String source = "OSM";
}
