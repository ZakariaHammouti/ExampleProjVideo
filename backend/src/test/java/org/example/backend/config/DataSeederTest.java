package org.example.backend.config;

import org.example.backend.model.Restaurant;
import org.example.backend.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataSeederTest {

    private final RestaurantRepository repository = mock(RestaurantRepository.class);
    private final DataSeeder seeder = new DataSeeder(repository);

    @Test
    void sampleRestaurants_are30WithUniqueNamesAndValidValues() {
        List<Restaurant> samples = DataSeeder.sampleRestaurants();
        assertEquals(30, samples.size());
        assertEquals(30, samples.stream().map(Restaurant::getName).collect(Collectors.toSet()).size());
        samples.forEach(r -> {
            assertTrue(r.getRating() >= 0 && r.getRating() <= 5, r.getName());
            assertTrue(r.getPriceLevel() >= 1 && r.getPriceLevel() <= 4, r.getName());
            assertEquals("Köln", r.getCity());
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void run_emptyDatabase_saves30() {
        when(repository.findAll()).thenReturn(List.of());

        seeder.run();

        ArgumentCaptor<List<Restaurant>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(30, captor.getValue().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void run_sixAlreadyExist_savesOnlyMissing24() {
        List<Restaurant> existing = DataSeeder.sampleRestaurants().subList(0, 6);
        when(repository.findAll()).thenReturn(existing);

        seeder.run();

        ArgumentCaptor<List<Restaurant>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(24, captor.getValue().size());
        Set<String> existingNames = existing.stream().map(Restaurant::getName).collect(Collectors.toSet());
        assertTrue(captor.getValue().stream().noneMatch(r -> existingNames.contains(r.getName())));
    }

    @Test
    void run_allExist_savesNothing() {
        when(repository.findAll()).thenReturn(DataSeeder.sampleRestaurants());

        seeder.run();

        verify(repository, never()).saveAll(any());
    }
}
