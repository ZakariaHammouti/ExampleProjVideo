package org.example.backend.service;

import org.example.backend.model.NearbyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Testet PlacesService ohne echtes Internet: MockRestServiceServer spielt OpenStreetMap.
 */
class PlacesServiceTest {

    private static final String NOMINATIM = "https://nominatim.test";
    private static final String OVERPASS = "https://overpass.test/api/interpreter";
    private static final String OVERPASS_MIRROR = "https://overpass-mirror.test/api/interpreter";

    private MockRestServiceServer server;
    private PlacesService service;

    // Ein Restaurant als Punkt (node), eins als Gebäude (way, mit center), eins ohne Namen (fällt weg)
    private static final String OVERPASS_JSON = """
            {"elements": [
              {"type": "node", "id": 1, "lat": 50.9400, "lon": 6.9600,
               "tags": {"amenity": "restaurant", "name": "Weit weg", "cuisine": "italian;pizza",
                        "addr:street": "Domkloster", "addr:housenumber": "4", "addr:city": "Köln",
                        "opening_hours": "Mo-Su 11:00-23:00"}},
              {"type": "way", "id": 2, "center": {"lat": 50.9376, "lon": 6.9604},
               "tags": {"amenity": "restaurant", "name": "Ganz nah"}},
              {"type": "node", "id": 3, "lat": 50.9377, "lon": 6.9605,
               "tags": {"amenity": "restaurant"}}
            ]}
            """;

    // Antwort der Nominatim-Restaurantsuche (Ausweichquelle); das zweite liegt außerhalb des Radius
    private static final String NOMINATIM_PLACES_JSON = """
            [
              {"osm_type": "node", "osm_id": 427539891, "name": "Haxenhaus", "lat": "50.9376", "lon": "6.9624",
               "extratags": {"cuisine": "regional;german", "opening_hours": "Mo-Su 11:00-24:00"},
               "address": {"road": "Frankenwerft", "house_number": "19", "city": "Köln"}},
              {"osm_type": "way", "osm_id": 5, "name": "Viel zu weit", "lat": "51.5", "lon": "7.5"}
            ]
            """;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new PlacesService(builder, NOMINATIM, List.of(OVERPASS, OVERPASS_MIRROR));
    }

    @Test
    void findNearbyLocation_mapsAndSortsByDistance() {
        server.expect(requestTo(OVERPASS))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("User-Agent", "RestaurantFinder/0.1 (Abschlussprojekt)"))
                .andRespond(withSuccess(OVERPASS_JSON, MediaType.APPLICATION_JSON));

        NearbyResponse result = service.findNearbyLocation(50.9375, 6.9603, 1);

        assertEquals("Dein Standort", result.getLocationName());
        assertEquals("Overpass", result.getProvider());
        assertEquals(2, result.getPlaces().size(), "Restaurant ohne Namen muss wegfallen");

        var first = result.getPlaces().get(0);
        assertEquals("Ganz nah", first.getName());
        assertEquals("osm-way-2", first.getId());
        assertNull(first.getCuisine());

        var second = result.getPlaces().get(1);
        assertEquals("Italienisch, Pizza", second.getCuisine());
        assertEquals("Domkloster 4", second.getAddress());
        assertEquals("Köln", second.getCity());
        assertEquals("Mo-Su 11:00-23:00", second.getOpeningHours());
        assertTrue(second.getDistanceKm() > first.getDistanceKm());
        server.verify();
    }

    @Test
    void findNearbyLocation_overpassSendsOctetStream_stillParsed() {
        server.expect(requestTo(OVERPASS))
                .andRespond(withSuccess(OVERPASS_JSON, MediaType.APPLICATION_OCTET_STREAM));

        assertEquals(2, service.findNearbyLocation(50.9375, 6.9603, 1).getPlaces().size());
    }

    @Test
    void findNearbyCity_geocodesFirst() {
        server.expect(requestTo(NOMINATIM + "/search?q=K%C3%B6ln&format=json&limit=1&accept-language=de"))
                .andRespond(withSuccess("""
                        [{"lat": "50.9375", "lon": "6.9603", "display_name": "Köln, Deutschland"}]
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(OVERPASS))
                .andRespond(withSuccess(OVERPASS_JSON, MediaType.APPLICATION_JSON));

        NearbyResponse result = service.findNearbyCity("Köln", 1);

        assertEquals("Köln, Deutschland", result.getLocationName());
        assertEquals(50.9375, result.getLatitude());
        assertEquals(2, result.getPlaces().size());
        server.verify();
    }

    @Test
    void findNearbyCity_unknownCity_throws404() {
        server.expect(requestTo(NOMINATIM + "/search?q=Xyzabc&format=json&limit=1&accept-language=de"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var ex = assertThrows(ResponseStatusException.class, () -> service.findNearbyCity("Xyzabc", 1));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void findNearbyLocation_firstServerDown_usesMirror() {
        server.expect(requestTo(OVERPASS)).andRespond(withServerError());
        server.expect(requestTo(OVERPASS_MIRROR))
                .andRespond(withSuccess(OVERPASS_JSON, MediaType.APPLICATION_JSON));

        NearbyResponse result = service.findNearbyLocation(50.9375, 6.9603, 1);

        assertEquals("Overpass", result.getProvider());
        assertEquals(2, result.getPlaces().size());
        server.verify();
    }

    @Test
    void findNearbyLocation_allOverpassDown_fallsBackToNominatim() {
        server.expect(requestTo(OVERPASS)).andRespond(withServerError());
        server.expect(requestTo(OVERPASS_MIRROR)).andRespond(withSuccess("<html>busy</html>", MediaType.TEXT_HTML));
        server.expect(requestTo(startsWith(NOMINATIM + "/search?amenity=restaurant")))
                .andExpect(queryParam("bounded", "1"))
                .andExpect(queryParam("extratags", "1"))
                .andRespond(withSuccess(NOMINATIM_PLACES_JSON, MediaType.APPLICATION_JSON));

        NearbyResponse result = service.findNearbyLocation(50.9375, 6.9603, 1);

        assertEquals("Nominatim", result.getProvider());
        assertEquals(1, result.getPlaces().size(), "Treffer außerhalb des Radius muss wegfallen");
        var place = result.getPlaces().getFirst();
        assertEquals("Haxenhaus", place.getName());
        assertEquals("osm-node-427539891", place.getId());
        assertEquals("Regional, Deutsch", place.getCuisine());
        assertEquals("Frankenwerft 19", place.getAddress());
        assertEquals("Köln", place.getCity());
        server.verify();
    }

    @Test
    void findNearbyLocation_everythingDown_throws502() {
        server.expect(requestTo(OVERPASS)).andRespond(withServerError());
        server.expect(requestTo(OVERPASS_MIRROR)).andRespond(withServerError());
        server.expect(requestTo(startsWith(NOMINATIM))).andRespond(withServerError());

        var ex = assertThrows(ResponseStatusException.class, () -> service.findNearbyLocation(50.9, 6.9, 1));
        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }
}
