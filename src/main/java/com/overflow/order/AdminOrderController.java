package com.overflow.order;

import com.overflow.order.dto.AssignPartnerRequest;
import com.overflow.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orders;

    public AdminOrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public List<OrderResponse> list() { return orders.allOrders(); }

    @PutMapping("/{orderId}/assignment")
    public OrderResponse assign(@PathVariable Long orderId, @Valid @RequestBody AssignPartnerRequest request) {
        return orders.assignPartner(orderId, request.partnerId());
    }
}
