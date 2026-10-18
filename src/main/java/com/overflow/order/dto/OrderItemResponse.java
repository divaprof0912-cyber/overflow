package com.overflow.order.dto;

import com.overflow.order.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(Long foodItemId, String itemName, BigDecimal unitPrice,
                                int quantity, BigDecimal lineTotal) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getFoodItemId(), item.getItemName(), item.getUnitPrice(),
                item.getQuantity(), item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}
