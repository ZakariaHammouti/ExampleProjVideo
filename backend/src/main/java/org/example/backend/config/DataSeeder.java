package org.example.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.model.Restaurant;
import org.example.backend.repository.RestaurantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Legt beim Start 30 erfundene Beispiel-Restaurants in Köln an.
 * Es werden nur Restaurants ergänzt, deren Name noch nicht in der Datenbank steht –
 * mehrfaches Starten erzeugt also keine Duplikate.
 * Abschaltbar mit app.seed-data=false.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;

    @Override
    public void run(String... args) {
        Set<String> existingNames = restaurantRepository.findAll().stream()
                .map(Restaurant::getName)
                .collect(Collectors.toSet());

        List<Restaurant> missing = sampleRestaurants().stream()
                .filter(r -> !existingNames.contains(r.getName()))
                .toList();

        if (missing.isEmpty()) {
            return;
        }
        restaurantRepository.saveAll(missing);
        log.info("{} Beispiel-Restaurants in MongoDB angelegt", missing.size());
    }

    static List<Restaurant> sampleRestaurants() {
        return List.of(
                restaurant("Trattoria Bella", "Italienisch", "Hauptstraße 12", 4.6, 2, 50.9375, 6.9603, true),
                restaurant("Sakura Sushi", "Japanisch", "Ehrenstraße 45", 4.4, 3, 50.9390, 6.9420, true),
                restaurant("Burger Werk", "Amerikanisch", "Zülpicher Str. 8", 4.1, 1, 50.9300, 6.9380, false),
                restaurant("Istanbul Grill", "Türkisch", "Keupstraße 30", 4.7, 1, 50.9620, 7.0080, true),
                restaurant("Le Petit Bistro", "Französisch", "Brüsseler Platz 3", 4.5, 3, 50.9355, 6.9365, false),
                restaurant("Curry Palace", "Indisch", "Venloer Str. 101", 4.3, 2, 50.9480, 6.9250, true),
                restaurant("Zum Goldenen Fass", "Deutsch", "Severinstraße 87", 4.2, 2, 50.9265, 6.9590, true),
                restaurant("Pasta Fresca", "Italienisch", "Aachener Str. 214", 4.5, 2, 50.9370, 6.9180, true),
                restaurant("Pho Hanoi Kitchen", "Vietnamesisch", "Luxemburger Str. 56", 4.6, 1, 50.9250, 6.9330, true),
                restaurant("Taverna Kreta", "Griechisch", "Neusser Str. 140", 4.3, 2, 50.9560, 6.9590, false),
                restaurant("El Toro Loco", "Mexikanisch", "Lindenstraße 19", 3.9, 2, 50.9330, 6.9400, true),
                restaurant("Bangkok Garden", "Thailändisch", "Deutzer Freiheit 72", 4.4, 2, 50.9365, 6.9750, true),
                restaurant("Seoul BBQ House", "Koreanisch", "Hohe Straße 120", 4.5, 3, 50.9380, 6.9555, false),
                restaurant("Casa Iberica", "Spanisch", "Friesenwall 33", 4.2, 3, 50.9405, 6.9415, true),
                restaurant("Cedar Tree", "Libanesisch", "Bonner Str. 210", 4.6, 1, 50.9145, 6.9620, true),
                restaurant("Steakhaus Rheinblick", "Steakhaus", "Frankenwerft 7", 4.0, 4, 50.9375, 6.9635, true),
                restaurant("Fischerhütte am Hafen", "Fisch & Meeresfrüchte", "Rheinauhafen 9", 4.3, 3, 50.9270, 6.9665, false),
                restaurant("Grünkern", "Vegetarisch", "Brüsseler Str. 61", 4.7, 2, 50.9375, 6.9350, true),
                restaurant("Planted Bowl", "Vegan", "Körnerstraße 18", 4.5, 2, 50.9500, 6.9230, true),
                restaurant("Dragon Wok", "Chinesisch", "Weidengasse 41", 3.8, 1, 50.9495, 6.9555, true),
                restaurant("Napoli Forno", "Pizza", "Kalker Hauptstr. 155", 4.4, 1, 50.9375, 7.0040, true),
                restaurant("Bombay Spice", "Indisch", "Eigelstein 92", 4.1, 2, 50.9480, 6.9560, false),
                restaurant("Brauhaus Am Turm", "Regional", "Sülzburgstr. 104", 4.3, 2, 50.9180, 6.9220, true),
                restaurant("Sushi Kaito", "Japanisch", "Neumarkt 18", 4.8, 4, 50.9360, 6.9475, true),
                restaurant("Anatolia Lokanta", "Türkisch", "Berliner Str. 36", 4.5, 1, 50.9640, 7.0120, true),
                restaurant("Bistro Lumière", "Französisch", "Dürener Str. 190", 4.6, 4, 50.9310, 6.9130, false),
                restaurant("Smoky Joe's BBQ", "Amerikanisch", "Ehrenfeldgürtel 88", 4.2, 2, 50.9460, 6.9170, true),
                restaurant("Saigon Street", "Vietnamesisch", "Bonner Wall 15", 4.0, 1, 50.9195, 6.9595, true),
                restaurant("Olivo", "Mediterran", "Sachsenring 47", 4.4, 3, 50.9255, 6.9470, true),
                restaurant("Alt Kölsche Stuuv", "Deutsch", "Alter Markt 10", 4.1, 2, 50.9385, 6.9590, true)
        );
    }

    private static Restaurant restaurant(String name, String cuisine, String address, double rating,
                                         int priceLevel, double latitude, double longitude, boolean openNow) {
        return Restaurant.builder()
                .name(name)
                .cuisine(cuisine)
                .address(address)
                .city("Köln")
                .rating(rating)
                .priceLevel(priceLevel)
                .latitude(latitude)
                .longitude(longitude)
                .openNow(openNow)
                .build();
    }
}
