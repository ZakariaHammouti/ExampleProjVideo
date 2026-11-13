package org.example.backend.service;

import org.example.backend.model.Restaurant;
import org.example.backend.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestaurantServiceTest {

    private final RestaurantRepository repository = mock(RestaurantRepository.class);
    private final RestaurantService service = new RestaurantService(repository);

    private final Restaurant sushi = Restaurant.builder().id("1").name("Sakura Sushi")
            .cuisine("Japanisch").address("Ehrenstraße 45").rating(4.4).build();
    private final Restaurant grill = Restaurant.builder().id("2").name("Istanbul Grill")
            .cuisine("Türkisch").address("Keupstraße 30").rating(4.7).build();

    @BeforeEach
    void setUp() {
        when(repository.findAll()).thenReturn(List.of(sushi, grill));
    }

    @Test
    void findAll_withoutFilter_returnsAllSortedByRating() {
        assertEquals(List.of(grill, sushi), service.findAll(null, null));
    }

    @Test
    void findAll_withSearch_filtersByName() {
        assertEquals(List.of(sushi), service.findAll("sushi", null));
    }

    @Test
    void findAll_withCuisine_filtersByCuisine() {
        assertEquals(List.of(grill), service.findAll(null, "türkisch"));
    }

    @Test
    void findAllCuisines_returnsDistinctSorted() {
        assertEquals(List.of("Japanisch", "Türkisch"), service.findAllCuisines());
    }

    @Test
    void findById_unknownId_throws() {
        when(repository.findById("999")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.findById("999"));
    }
}
