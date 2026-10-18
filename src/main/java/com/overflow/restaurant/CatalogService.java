package com.overflow.restaurant;

import com.overflow.common.ApiException;
import com.overflow.restaurant.dto.FoodItemRequest;
import com.overflow.restaurant.dto.FoodItemResponse;
import com.overflow.restaurant.dto.RestaurantRequest;
import com.overflow.restaurant.dto.RestaurantResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {
    private final RestaurantRepository restaurants;
    private final FoodItemRepository foodItems;

    public CatalogService(RestaurantRepository restaurants, FoodItemRepository foodItems) {
        this.restaurants = restaurants;
        this.foodItems = foodItems;
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> browseRestaurants() {
        return restaurants.findAllByActiveTrueOrderByNameAsc().stream().map(RestaurantResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse browseRestaurant(Long id) {
        return RestaurantResponse.from(restaurants.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found")));
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> browseMenu(Long restaurantId) {
        if (!restaurants.findByIdAndActiveTrue(restaurantId).isPresent()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found");
        }
        return foodItems.findPublicMenu(restaurantId).stream().map(FoodItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> listMenuForAdmin(Long restaurantId) {
        requireRestaurant(restaurantId);
        return foodItems.findAllByRestaurant_IdOrderByNameAsc(restaurantId)
                .stream().map(FoodItemResponse::from).toList();
    }

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = new Restaurant(clean(request.name()), blankToNull(request.description()),
                clean(request.address()), blankToNull(request.phone()));
        return RestaurantResponse.from(restaurants.save(restaurant));
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = requireRestaurant(id);
        restaurant.update(clean(request.name()), blankToNull(request.description()), clean(request.address()),
                blankToNull(request.phone()));
        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public void deactivateRestaurant(Long id) {
        requireRestaurant(id).deactivate();
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> listRestaurantsForAdmin() {
        return restaurants.findAll().stream().map(RestaurantResponse::from).toList();
    }

    @Transactional
    public FoodItemResponse createFoodItem(Long restaurantId, FoodItemRequest request) {
        Restaurant restaurant = requireRestaurant(restaurantId);
        if (!restaurant.isActive()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot add menu items to an inactive restaurant");
        }
        FoodItem item = new FoodItem(restaurant, clean(request.name()), blankToNull(request.description()),
                request.price(), request.available());
        return FoodItemResponse.from(foodItems.save(item));
    }

    @Transactional
    public FoodItemResponse updateFoodItem(Long id, FoodItemRequest request) {
        FoodItem item = foodItems.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Food item not found"));
        item.update(clean(request.name()), blankToNull(request.description()), request.price(), request.available());
        return FoodItemResponse.from(item);
    }

    @Transactional
    public void deactivateFoodItem(Long id) {
        FoodItem item = foodItems.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Food item not found"));
        item.deactivate();
    }

    private Restaurant requireRestaurant(Long id) {
        return restaurants.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found"));
    }

    private String clean(String value) { return value.trim(); }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
