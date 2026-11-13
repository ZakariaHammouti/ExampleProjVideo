package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.model.Restaurant;
import org.example.backend.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public List<Restaurant> getAll(@RequestParam(required = false) String search,
                                   @RequestParam(required = false) String cuisine) {
        return restaurantService.findAll(search, cuisine);
    }

    @GetMapping("/cuisines")
    public List<String> getCuisines() {
        return restaurantService.findAllCuisines();
    }

    @GetMapping("/{id}")
    public Restaurant getById(@PathVariable String id) {
        return restaurantService.findById(id);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException e) {
        return e.getMessage();
    }
}
