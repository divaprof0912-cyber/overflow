package com.overflow.order;

import com.overflow.common.ApiException;
import com.overflow.order.dto.CreateOrderRequest;
import com.overflow.order.dto.OrderItemResponse;
import com.overflow.order.dto.OrderResponse;
import com.overflow.restaurant.FoodItem;
import com.overflow.restaurant.FoodItemRepository;
import com.overflow.restaurant.Restaurant;
import com.overflow.user.AppUser;
import com.overflow.user.UserRole;
import com.overflow.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final CustomerOrderRepository orders;
    private final FoodItemRepository foodItems;
    private final UserService users;

    public OrderService(CustomerOrderRepository orders, FoodItemRepository foodItems, UserService users) {
        this.orders = orders;
        this.foodItems = foodItems;
        this.users = users;
    }

    @Transactional
    public OrderResponse placeOrder(AppUser customer, CreateOrderRequest request) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (CreateOrderRequest.Line line : request.items()) {
            quantities.merge(line.foodItemId(), line.quantity(), Math::addExact);
        }

        List<FoodItem> found = foodItems.findAllById(quantities.keySet());
        if (found.size() != quantities.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "One or more food items were not found");
        }
        Map<Long, FoodItem> byId = found.stream().collect(Collectors.toMap(FoodItem::getId, item -> item));
        Restaurant restaurant = null;
        BigDecimal total = BigDecimal.ZERO;
        CustomerOrder order = null;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            FoodItem item = byId.get(entry.getKey());
            if (!item.isAvailable() || !item.getRestaurant().isActive()) {
                throw new ApiException(HttpStatus.CONFLICT, "A selected food item is no longer available");
            }
            if (restaurant == null) {
                restaurant = item.getRestaurant();
                order = new CustomerOrder(customer, restaurant, request.deliveryAddress().trim(), clean(request.notes()));
            } else if (!restaurant.getId().equals(item.getRestaurant().getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "An order can contain items from only one restaurant");
            }
            int quantity = entry.getValue();
            order.addItem(new OrderItem(item.getId(), item.getName(), item.getPrice(), quantity));
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalAmount(total);
        return toResponse(orders.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> customerOrders(AppUser customer) {
        return orders.findAllByCustomer_IdOrderByCreatedAtDesc(customer.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getCustomerOrder(Long id, AppUser customer) {
        CustomerOrder order;
        if (customer.getRole() == UserRole.ADMIN) {
            order = requireOrder(id);
        } else {
            order = orders.findByIdAndCustomer_Id(id, customer.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders() {
        return orders.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse assignPartner(Long orderId, Long partnerId) {
        CustomerOrder order = requireOrder(orderId);
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.ASSIGNED) {
            throw new ApiException(HttpStatus.CONFLICT, "Only new or not-yet-started orders can be assigned");
        }
        AppUser partner = users.requirePartner(partnerId);
        order.assignPartner(partner);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> assignedDeliveries(AppUser partner) {
        return orders.findAllByPartner_IdOrderByCreatedAtDesc(partner.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse updateDeliveryStatus(Long orderId, AppUser partner, OrderStatus nextStatus) {
        CustomerOrder order = requireOrder(orderId);
        if (order.getPartner() == null || !order.getPartner().getId().equals(partner.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Assigned delivery not found");
        }
        boolean validTransition = switch (order.getStatus()) {
            case ASSIGNED -> nextStatus == OrderStatus.PICKED_UP;
            case PICKED_UP -> nextStatus == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> nextStatus == OrderStatus.DELIVERED;
            default -> false;
        };
        if (!validTransition) {
            throw new ApiException(HttpStatus.CONFLICT, "Delivery status must progress from assigned to picked up, out for delivery, then delivered");
        }
        order.updateStatus(nextStatus);
        return toResponse(order);
    }

    private CustomerOrder requireOrder(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        AppUser partner = order.getPartner();
        return new OrderResponse(order.getId(), order.getCustomer().getId(), order.getCustomer().getName(),
                order.getRestaurant().getId(), order.getRestaurant().getName(),
                partner == null ? null : partner.getId(), partner == null ? null : partner.getName(),
                order.getStatus(), order.getTotalAmount(), order.getDeliveryAddress(), order.getNotes(),
                order.getCreatedAt(), order.getUpdatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
