package com.overflow.order;

import com.overflow.order.dto.CreateOrderRequest;
import com.overflow.order.dto.OrderResponse;
import com.overflow.user.AppUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {
    private final OrderService orders;

    public CustomerOrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@AuthenticationPrincipal AppUser customer,
                                    @Valid @RequestBody CreateOrderRequest request) {
        return orders.placeOrder(customer, request);
    }

    @GetMapping("/mine")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal AppUser customer) {
        return orders.customerOrders(customer);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id, @AuthenticationPrincipal AppUser customer) {
        return orders.getCustomerOrder(id, customer);
    }
}
