package com.overflow.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findAllByActiveTrueOrderByNameAsc();
    Optional<Restaurant> findByIdAndActiveTrue(Long id);
}
