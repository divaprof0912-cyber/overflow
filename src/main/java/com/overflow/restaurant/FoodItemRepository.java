package com.overflow.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    @Query("select f from FoodItem f where f.restaurant.id = :restaurantId and f.available = true and f.restaurant.active = true order by f.name")
    List<FoodItem> findPublicMenu(@Param("restaurantId") Long restaurantId);

    List<FoodItem> findAllByRestaurant_IdOrderByNameAsc(Long restaurantId);

    Optional<FoodItem> findByIdAndRestaurant_Id(Long id, Long restaurantId);
}
