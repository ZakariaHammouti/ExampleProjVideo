package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.model.Restaurant;
import org.example.backend.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public List<Restaurant> findAll(String search, String cuisine) {
        return restaurantRepository.findAll().stream()
                .filter(r -> isBlank(search) || matches(r, search))
                .filter(r -> isBlank(cuisine) || r.getCuisine().equalsIgnoreCase(cuisine))
                .sorted(Comparator.comparingDouble(Restaurant::getRating).reversed())
                .toList();
    }

    public Restaurant findById(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Restaurant " + id + " nicht gefunden"));
    }

    public List<String> findAllCuisines() {
        return restaurantRepository.findAll().stream()
                .map(Restaurant::getCuisine).distinct().sorted().toList();
    }

    private boolean matches(Restaurant r, String search) {
        String term = search.toLowerCase(Locale.ROOT);
        return r.getName().toLowerCase(Locale.ROOT).contains(term)
                || r.getCuisine().toLowerCase(Locale.ROOT).contains(term)
                || r.getAddress().toLowerCase(Locale.ROOT).contains(term);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
