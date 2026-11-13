package org.example.backend.controller;

import org.example.backend.model.Restaurant;
import org.example.backend.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Kein echtes MongoDB im Test: Repository wird gemockt
    @MockitoBean
    private RestaurantRepository restaurantRepository;

    @Test
    void getAll_returnsRestaurants() throws Exception {
        when(restaurantRepository.findAll()).thenReturn(List.of(
                Restaurant.builder().id("1").name("Sakura Sushi").cuisine("Japanisch")
                        .address("Ehrenstraße 45").rating(4.4).build()));

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Sakura Sushi"));
    }

    @Test
    void getById_unknown_returns404() throws Exception {
        when(restaurantRepository.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/restaurants/999"))
                .andExpect(status().isNotFound());
    }
}
