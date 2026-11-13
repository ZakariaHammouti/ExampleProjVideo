package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.model.NearbyResponse;
import org.example.backend.service.GeoUtils;
import org.example.backend.service.PlacesService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Echte Restaurants aus OpenStreetMap.
 * Entweder per Stadt (?city=Köln) oder per Standort (?lat=..&lng=..).
 */
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlacesController {

    static final double MAX_RADIUS_KM = 5.0;
    static final double MIN_RADIUS_KM = 0.1;

    private final PlacesService placesService;

    @GetMapping("/nearby")
    public NearbyResponse nearby(@RequestParam(required = false) String city,
                                 @RequestParam(required = false) Double lat,
                                 @RequestParam(required = false) Double lng,
                                 @RequestParam(defaultValue = "2") double radiusKm) {
        if (radiusKm < MIN_RADIUS_KM || radiusKm > MAX_RADIUS_KM) {
            throw badRequest("radiusKm muss zwischen " + MIN_RADIUS_KM + " und " + MAX_RADIUS_KM + " liegen");
        }

        if (city != null && !city.isBlank()) {
            return placesService.findNearbyCity(city.trim(), radiusKm);
        }
        if (lat != null && lng != null) {
            if (!GeoUtils.isValid(lat, lng)) {
                throw badRequest("Ungültige Koordinaten");
            }
            return placesService.findNearbyLocation(lat, lng, radiusKm);
        }
        throw badRequest("Bitte entweder city oder lat und lng angeben");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
