package com.overflow.order.dto;

import com.overflow.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusRequest(@NotNull OrderStatus status) { }
