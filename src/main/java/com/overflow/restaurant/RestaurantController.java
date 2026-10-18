package com.overflow.restaurant;

import com.overflow.restaurant.dto.FoodItemResponse;
import com.overflow.restaurant.dto.RestaurantResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final CatalogService catalog;

    public RestaurantController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<RestaurantResponse> list() { return catalog.browseRestaurants(); }

    @GetMapping("/{id}")
    public RestaurantResponse get(@PathVariable Long id) { return catalog.browseRestaurant(id); }

    @GetMapping("/{id}/items")
    public List<FoodItemResponse> menu(@PathVariable Long id) { return catalog.browseMenu(id); }
}
