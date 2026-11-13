package org.example.backend;

import org.example.backend.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BackendApplicationTests {

    // Kein echtes MongoDB im Test: Repository wird gemockt
    @MockitoBean
    private RestaurantRepository restaurantRepository;

    @Test
    void contextLoads() {
    }

}
