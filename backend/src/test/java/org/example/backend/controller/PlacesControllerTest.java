package org.example.backend.controller;

import org.example.backend.model.NearbyResponse;
import org.example.backend.repository.RestaurantRepository;
import org.example.backend.service.PlacesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlacesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlacesService placesService;

    @MockitoBean
    private RestaurantRepository restaurantRepository;

    @Test
    void nearby_withCity_callsCitySearch() throws Exception {
        when(placesService.findNearbyCity("Köln", 2)).thenReturn(NearbyResponse.builder()
                .locationName("Köln, Deutschland").radiusKm(2).places(List.of()).build());

        mockMvc.perform(get("/api/places/nearby").param("city", "Köln"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationName").value("Köln, Deutschland"));
    }

    @Test
    void nearby_withLocation_callsLocationSearch() throws Exception {
        when(placesService.findNearbyLocation(50.9, 6.9, 1)).thenReturn(NearbyResponse.builder()
                .locationName("Dein Standort").radiusKm(1).places(List.of()).build());

        mockMvc.perform(get("/api/places/nearby").param("lat", "50.9").param("lng", "6.9").param("radiusKm", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationName").value("Dein Standort"));
    }

    @Test
    void nearby_withoutParams_returns400() throws Exception {
        mockMvc.perform(get("/api/places/nearby")).andExpect(status().isBadRequest());
        verifyNoInteractions(placesService);
    }

    @Test
    void nearby_radiusTooLarge_returns400() throws Exception {
        mockMvc.perform(get("/api/places/nearby").param("city", "Köln").param("radiusKm", "50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nearby_invalidCoordinates_returns400() throws Exception {
        mockMvc.perform(get("/api/places/nearby").param("lat", "200").param("lng", "6.9"))
                .andExpect(status().isBadRequest());
    }
}
