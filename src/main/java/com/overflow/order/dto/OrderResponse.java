package com.overflow.order.dto;

import com.overflow.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, Long customerId, String customerName, Long restaurantId,
                            String restaurantName, Long partnerId, String partnerName,
                            OrderStatus status, BigDecimal totalAmount, String deliveryAddress,
                            String notes, Instant createdAt, Instant updatedAt,
                            List<OrderItemResponse> items) { }
