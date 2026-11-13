package org.example.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.model.NearbyResponse;
import org.example.backend.model.Place;
import org.example.backend.model.osm.NominatimPlace;
import org.example.backend.model.osm.NominatimResult;
import org.example.backend.model.osm.OverpassResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Fragt echte Restaurants bei OpenStreetMap ab (kostenlos, kein API-Key).
 * <ol>
 *   <li>Nominatim: Stadtname -> Koordinaten (nur bei Suche nach Stadt)</li>
 *   <li>Overpass: alle Restaurants im Umkreis (vollständig, aber öffentliche Server oft überlastet)</li>
 *   <li>Fällt Overpass aus: Nominatim-Restaurantsuche als Ausweichquelle (schnell, max. 40 Treffer)</li>
 * </ol>
 */
@Slf4j
@Service
public class PlacesService {

    static final int MAX_RESULTS = 50;
    static final int NOMINATIM_LIMIT = 40;
    private static final String USER_AGENT = "RestaurantFinder/0.1 (Abschlussprojekt)";
    /** Nutzungsregel von Nominatim: höchstens 1 Anfrage pro Sekunde */
    private static final long NOMINATIM_MIN_INTERVAL_MS = 1000;

    private final RestClient restClient;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final String nominatimUrl;
    private final List<String> overpassUrls;
    private long lastNominatimCall = 0;

    public PlacesService(RestClient.Builder restClientBuilder,
                         @Value("${app.osm.nominatim-url}") String nominatimUrl,
                         @Value("${app.osm.overpass-urls}") List<String> overpassUrls) {
        // OpenStreetMap verlangt, dass sich jede Anwendung mit einem User-Agent zu erkennen gibt
        this.restClient = restClientBuilder.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT).build();
        this.nominatimUrl = nominatimUrl;
        this.overpassUrls = overpassUrls;
    }

    /** Suche nach Stadtname: erst Koordinaten ermitteln, dann Restaurants im Umkreis suchen. */
    public NearbyResponse findNearbyCity(String city, double radiusKm) {
        NominatimResult location = geocode(city);
        double lat = Double.parseDouble(location.lat());
        double lng = Double.parseDouble(location.lon());
        return search(lat, lng, location.displayName(), radiusKm);
    }

    /** Suche nach Koordinaten (z. B. Standort aus dem Browser). */
    public NearbyResponse findNearbyLocation(double lat, double lng, double radiusKm) {
        return search(lat, lng, "Dein Standort", radiusKm);
    }

    private NearbyResponse search(double lat, double lng, String locationName, double radiusKm) {
        String provider = "Overpass";
        Stream<Place> places;
        try {
            places = queryOverpass(lat, lng, radiusKm).stream().map(e -> toPlace(e, lat, lng)).flatMap(Optional::stream);
        } catch (OverpassUnavailableException e) {
            log.warn("Overpass nicht verfügbar, nutze Nominatim als Ausweichquelle");
            provider = "Nominatim";
            places = queryNominatimRestaurants(lat, lng, radiusKm).stream().map(p -> toPlace(p, lat, lng)).flatMap(Optional::stream);
        }

        List<Place> result = places
                .filter(p -> p.getDistanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(Place::getDistanceKm))
                .limit(MAX_RESULTS)
                .toList();

        return NearbyResponse.builder()
                .latitude(lat)
                .longitude(lng)
                .locationName(locationName)
                .provider(provider)
                .radiusKm(radiusKm)
                .places(result)
                .build();
    }

    // ---------- Nominatim ----------

    private NominatimResult geocode(String city) {
        NominatimResult[] results;
        try {
            throttleNominatim();
            results = restClient.get()
                    .uri(nominatimUrl + "/search?q={q}&format=json&limit=1&accept-language=de", city)
                    .retrieve()
                    .body(NominatimResult[].class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Ortssuche (OpenStreetMap Nominatim) ist gerade nicht erreichbar", e);
        }
        if (results == null || results.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ort \"" + city + "\" nicht gefunden");
        }
        return results[0];
    }

    private List<NominatimPlace> queryNominatimRestaurants(double lat, double lng, double radiusKm) {
        // Rechteck (viewbox) um den Suchpunkt: 1 Breitengrad ≈ 111,32 km
        double dLat = radiusKm / 111.32;
        double dLng = radiusKm / (111.32 * Math.cos(Math.toRadians(lat)));
        String viewbox = String.format(Locale.ROOT, "%.6f,%.6f,%.6f,%.6f", lng - dLng, lat + dLat, lng + dLng, lat - dLat);
        try {
            throttleNominatim();
            NominatimPlace[] results = restClient.get()
                    .uri(nominatimUrl + "/search?amenity=restaurant&format=jsonv2&limit={limit}&bounded=1"
                            + "&viewbox={viewbox}&extratags=1&addressdetails=1&accept-language=de",
                            NOMINATIM_LIMIT, viewbox)
                    .retrieve()
                    .body(NominatimPlace[].class);
            return results == null ? List.of() : List.of(results);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "OpenStreetMap ist gerade nicht erreichbar. Bitte später nochmal versuchen.", e);
        }
    }

    /** Wartet, falls die letzte Nominatim-Anfrage weniger als 1 Sekunde her ist. */
    private synchronized void throttleNominatim() {
        long wait = lastNominatimCall + NOMINATIM_MIN_INTERVAL_MS - System.currentTimeMillis();
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastNominatimCall = System.currentTimeMillis();
    }

    // ---------- Overpass ----------

    private List<OverpassResponse.Element> queryOverpass(double lat, double lng, double radiusKm) {
        int radiusMeters = (int) Math.round(radiusKm * 1000);
        // Overpass QL: alle Punkte (node) und Gebäude (way) mit amenity=restaurant und Namen im Umkreis
        String query = String.format(Locale.ROOT, """
                [out:json][timeout:10];
                (
                  node["amenity"="restaurant"]["name"](around:%d,%.6f,%.6f);
                  way["amenity"="restaurant"]["name"](around:%d,%.6f,%.6f);
                );
                out center tags;
                """, radiusMeters, lat, lng, radiusMeters, lat, lng);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("data", query);

        for (String url : overpassUrls) {
            try {
                // Als Text lesen: manche Overpass-Server melden "application/octet-stream" statt JSON
                String body = restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(form)
                        .retrieve()
                        .body(String.class);
                if (body == null || !body.stripLeading().startsWith("{")) {
                    log.warn("Overpass-Server {} lieferte kein JSON", url);
                    continue;
                }
                OverpassResponse response = jsonMapper.readValue(body, OverpassResponse.class);
                return response.elements() == null ? List.of() : response.elements();
            } catch (RestClientException | JacksonException e) {
                log.warn("Overpass-Server {} nicht verfügbar: {}", url, e.getMessage());
            }
        }
        throw new OverpassUnavailableException();
    }

    private static class OverpassUnavailableException extends RuntimeException {
    }

    // ---------- Umwandlung in Place ----------

    /** OSM-Element von Overpass -> Place. Elemente ohne Name oder Position fallen weg. */
    Optional<Place> toPlace(OverpassResponse.Element element, double fromLat, double fromLng) {
        Map<String, String> tags = element.tags() == null ? Map.of() : element.tags();
        Double lat = element.lat() != null ? element.lat() : element.center() != null ? element.center().lat() : null;
        Double lng = element.lon() != null ? element.lon() : element.center() != null ? element.center().lon() : null;
        if (lat == null || lng == null) {
            return Optional.empty();
        }
        return buildPlace("osm-" + element.type() + "-" + element.id(), tags.get("name"), lat, lng,
                tags.get("cuisine"), joinAddress(tags.get("addr:street"), tags.get("addr:housenumber")),
                tags.get("addr:city"), tags.get("opening_hours"),
                tags.getOrDefault("website", tags.get("contact:website")), fromLat, fromLng);
    }

    /** Restaurant von Nominatim -> Place. */
    Optional<Place> toPlace(NominatimPlace p, double fromLat, double fromLng) {
        Map<String, String> extra = p.extratags() == null ? Map.of() : p.extratags();
        Map<String, String> addr = p.address() == null ? Map.of() : p.address();
        if (p.lat() == null || p.lon() == null) {
            return Optional.empty();
        }
        String city = addr.getOrDefault("city", addr.getOrDefault("town", addr.get("village")));
        return buildPlace("osm-" + p.osmType() + "-" + p.osmId(), p.name(),
                Double.parseDouble(p.lat()), Double.parseDouble(p.lon()),
                extra.get("cuisine"), joinAddress(addr.get("road"), addr.get("house_number")), city,
                extra.get("opening_hours"), extra.getOrDefault("website", extra.get("contact:website")),
                fromLat, fromLng);
    }

    private Optional<Place> buildPlace(String id, String name, double lat, double lng, String osmCuisine,
                                       String address, String city, String openingHours, String website,
                                       double fromLat, double fromLng) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        double distance = GeoUtils.distanceKm(fromLat, fromLng, lat, lng);
        return Optional.of(Place.builder()
                .id(id)
                .name(name)
                .cuisine(CuisineTranslator.translate(osmCuisine))
                .address(address)
                .city(city)
                .latitude(lat)
                .longitude(lng)
                .distanceKm(Math.round(distance * 100) / 100.0)
                .openingHours(openingHours)
                .website(website)
                .build());
    }

    private String joinAddress(String street, String number) {
        if (street == null) {
            return null;
        }
        return number == null ? street : street + " " + number;
    }
}
