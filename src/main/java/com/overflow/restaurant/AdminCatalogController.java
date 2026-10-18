package com.overflow.restaurant;

import com.overflow.restaurant.dto.FoodItemRequest;
import com.overflow.restaurant.dto.FoodItemResponse;
import com.overflow.restaurant.dto.RestaurantRequest;
import com.overflow.restaurant.dto.RestaurantResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminCatalogController {
    private final CatalogService catalog;

    public AdminCatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/restaurants")
    public List<RestaurantResponse> listRestaurants() { return catalog.listRestaurantsForAdmin(); }

    @PostMapping("/restaurants")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        return catalog.createRestaurant(request);
    }

    @PutMapping("/restaurants/{id}")
    public RestaurantResponse updateRestaurant(@PathVariable Long id, @Valid @RequestBody RestaurantRequest request) {
        return catalog.updateRestaurant(id, request);
    }

    @GetMapping("/restaurants/{restaurantId}/items")
    public List<FoodItemResponse> listRestaurantMenu(@PathVariable Long restaurantId) {
        return catalog.listMenuForAdmin(restaurantId);
    }

    @DeleteMapping("/restaurants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateRestaurant(@PathVariable Long id) { catalog.deactivateRestaurant(id); }

    @PostMapping("/restaurants/{restaurantId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodItemResponse createFoodItem(@PathVariable Long restaurantId,
                                           @Valid @RequestBody FoodItemRequest request) {
        return catalog.createFoodItem(restaurantId, request);
    }

    @PutMapping("/items/{id}")
    public FoodItemResponse updateFoodItem(@PathVariable Long id, @Valid @RequestBody FoodItemRequest request) {
        return catalog.updateFoodItem(id, request);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateFoodItem(@PathVariable Long id) { catalog.deactivateFoodItem(id); }
}
