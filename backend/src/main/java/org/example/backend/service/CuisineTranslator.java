package org.example.backend.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Übersetzt OSM-Küchen ("italian;pizza") ins Deutsche ("Italienisch, Pizza").
 */
public final class CuisineTranslator {

    private static final Map<String, String> GERMAN = Map.ofEntries(
            Map.entry("italian", "Italienisch"),
            Map.entry("pizza", "Pizza"),
            Map.entry("japanese", "Japanisch"),
            Map.entry("sushi", "Sushi"),
            Map.entry("chinese", "Chinesisch"),
            Map.entry("indian", "Indisch"),
            Map.entry("turkish", "Türkisch"),
            Map.entry("kebab", "Döner/Kebab"),
            Map.entry("greek", "Griechisch"),
            Map.entry("french", "Französisch"),
            Map.entry("german", "Deutsch"),
            Map.entry("regional", "Regional"),
            Map.entry("american", "Amerikanisch"),
            Map.entry("burger", "Burger"),
            Map.entry("mexican", "Mexikanisch"),
            Map.entry("thai", "Thailändisch"),
            Map.entry("vietnamese", "Vietnamesisch"),
            Map.entry("asian", "Asiatisch"),
            Map.entry("korean", "Koreanisch"),
            Map.entry("spanish", "Spanisch"),
            Map.entry("portuguese", "Portugiesisch"),
            Map.entry("lebanese", "Libanesisch"),
            Map.entry("syrian", "Syrisch"),
            Map.entry("persian", "Persisch"),
            Map.entry("ethiopian", "Äthiopisch"),
            Map.entry("mediterranean", "Mediterran"),
            Map.entry("steak_house", "Steakhaus"),
            Map.entry("seafood", "Fisch & Meeresfrüchte"),
            Map.entry("vegetarian", "Vegetarisch"),
            Map.entry("vegan", "Vegan"),
            Map.entry("italian_pizza", "Pizza"),
            Map.entry("pasta", "Pasta"),
            Map.entry("salad", "Salate"),
            Map.entry("fish", "Fisch"),
            Map.entry("argentinian", "Argentinisch"),
            Map.entry("brazilian", "Brasilianisch"),
            Map.entry("peruvian", "Peruanisch"),
            Map.entry("polish", "Polnisch"),
            Map.entry("russian", "Russisch"),
            Map.entry("ukrainian", "Ukrainisch"),
            Map.entry("balkan", "Balkan"),
            Map.entry("croatian", "Kroatisch"),
            Map.entry("georgian", "Georgisch"),
            Map.entry("austrian", "Österreichisch"),
            Map.entry("bavarian", "Bayerisch"),
            Map.entry("swiss", "Schweizerisch"),
            Map.entry("african", "Afrikanisch"),
            Map.entry("international", "International"),
            Map.entry("tapas", "Tapas"),
            Map.entry("ramen", "Ramen"),
            Map.entry("noodle", "Nudeln"),
            Map.entry("dumpling", "Teigtaschen"),
            Map.entry("chicken", "Hähnchen"),
            Map.entry("sandwich", "Sandwiches"),
            Map.entry("breakfast", "Frühstück"),
            Map.entry("falafel", "Falafel"),
            Map.entry("friture", "Frittiertes"),
            Map.entry("pork_knuckle", "Haxe"),
            Map.entry("sausage", "Wurst")
    );

    private CuisineTranslator() {
    }

    public static String translate(String osmCuisine) {
        if (osmCuisine == null || osmCuisine.isBlank()) {
            return null;
        }
        return Arrays.stream(osmCuisine.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> GERMAN.getOrDefault(s.toLowerCase(Locale.ROOT), capitalize(s.replace('_', ' '))))
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
