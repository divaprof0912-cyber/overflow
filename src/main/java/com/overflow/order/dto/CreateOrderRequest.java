package com.overflow.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Size(max = 500) String deliveryAddress,
        @Size(max = 500) String notes,
        @NotEmpty @Size(max = 50) List<@Valid Line> items
) {
    public record Line(@NotNull Long foodItemId, @NotNull @Min(1) @Max(100) Integer quantity) { }
}
