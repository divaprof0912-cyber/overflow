package com.overflow.restaurant.dto;

import com.overflow.restaurant.FoodItem;

import java.math.BigDecimal;

public record FoodItemResponse(Long id, Long restaurantId, String restaurantName, String name,
                               String description, BigDecimal price, boolean available) {
    public static FoodItemResponse from(FoodItem item) {
        return new FoodItemResponse(item.getId(), item.getRestaurant().getId(), item.getRestaurant().getName(),
                item.getName(), item.getDescription(), item.getPrice(), item.isAvailable());
    }
}
