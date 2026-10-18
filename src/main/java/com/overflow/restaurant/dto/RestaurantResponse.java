package com.overflow.restaurant.dto;

import com.overflow.restaurant.Restaurant;

public record RestaurantResponse(Long id, String name, String description, String address,
                                 String phone, boolean active) {
    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getDescription(),
                restaurant.getAddress(), restaurant.getPhone(), restaurant.isActive());
    }
}
