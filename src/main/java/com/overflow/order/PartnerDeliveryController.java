package com.overflow.order;

import com.overflow.order.dto.DeliveryStatusRequest;
import com.overflow.order.dto.OrderResponse;
import com.overflow.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/partner/deliveries")
public class PartnerDeliveryController {
    private final OrderService orders;

    public PartnerDeliveryController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public List<OrderResponse> assigned(@AuthenticationPrincipal AppUser partner) {
        return orders.assignedDeliveries(partner);
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable Long orderId,
                                      @AuthenticationPrincipal AppUser partner,
                                      @Valid @RequestBody DeliveryStatusRequest request) {
        return orders.updateDeliveryStatus(orderId, partner, request.status());
    }
}
