package com.overflow.order;

import com.overflow.common.ApiException;
import com.overflow.order.dto.CreateOrderRequest;
import com.overflow.order.dto.OrderResponse;
import com.overflow.restaurant.FoodItem;
import com.overflow.restaurant.FoodItemRepository;
import com.overflow.restaurant.Restaurant;
import com.overflow.user.AppUser;
import com.overflow.user.UserRole;
import com.overflow.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    @Test
    void placeOrderUsesCurrentMenuPriceAndStoresItemSnapshot() throws Exception {
        Restaurant restaurant = restaurant(12L, "Overflow Kitchen");
        FoodItem item = foodItem(44L, restaurant, "Rice Bowl", "7.25");
        OrderService service = service(null, List.of(item), null);

        OrderResponse response = service.placeOrder(customer(), new CreateOrderRequest(
                "  10 Market Street  ", "  Ring bell  ", List.of(new CreateOrderRequest.Line(44L, 2))));

        assertEquals(OrderStatus.PLACED, response.status());
        assertEquals("10 Market Street", response.deliveryAddress());
        assertEquals("Ring bell", response.notes());
        assertEquals(new BigDecimal("14.50"), response.totalAmount());
        assertEquals(44L, response.items().getFirst().foodItemId());
        assertEquals("Rice Bowl", response.items().getFirst().itemName());
        assertEquals(new BigDecimal("7.25"), response.items().getFirst().unitPrice());
        assertEquals(new BigDecimal("14.50"), response.items().getFirst().lineTotal());
    }

    @Test
    void placeOrderRejectsItemsFromDifferentRestaurants() throws Exception {
        FoodItem first = foodItem(44L, restaurant(12L, "First Kitchen"), "Rice Bowl", "7.25");
        FoodItem second = foodItem(45L, restaurant(13L, "Second Kitchen"), "Soup", "4.00");
        OrderService service = service(null, List.of(first, second), null);

        ApiException exception = assertThrows(ApiException.class, () -> service.placeOrder(customer(),
                new CreateOrderRequest("10 Market Street", null, List.of(
                        new CreateOrderRequest.Line(44L, 1), new CreateOrderRequest.Line(45L, 1)))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void adminAssignmentSetsPartnerAndAssignedStatus() throws Exception {
        CustomerOrder order = new CustomerOrder(customer(), restaurant(12L, "Overflow Kitchen"),
                "10 Market Street", null);
        AppUser partner = user(80L, "Delivery Partner", UserRole.PARTNER);
        OrderService service = service(order, List.of(), partner);

        OrderResponse response = service.assignPartner(5L, 80L);

        assertEquals(OrderStatus.ASSIGNED, response.status());
        assertEquals(80L, response.partnerId());
        assertEquals("Delivery Partner", response.partnerName());
    }

    @Test
    void onlyAssignedPartnerCanAdvanceDeliveryThroughRequiredStatuses() throws Exception {
        CustomerOrder order = new CustomerOrder(customer(), restaurant(12L, "Overflow Kitchen"),
                "10 Market Street", null);
        AppUser assignedPartner = user(80L, "Assigned Partner", UserRole.PARTNER);
        AppUser otherPartner = user(81L, "Other Partner", UserRole.PARTNER);
        order.assignPartner(assignedPartner);
        OrderService service = service(order, List.of(), null);

        ApiException ownershipError = assertThrows(ApiException.class,
                () -> service.updateDeliveryStatus(5L, otherPartner, OrderStatus.PICKED_UP));
        assertEquals(HttpStatus.NOT_FOUND, ownershipError.getStatus());

        assertEquals(OrderStatus.PICKED_UP,
                service.updateDeliveryStatus(5L, assignedPartner, OrderStatus.PICKED_UP).status());
        assertEquals(OrderStatus.OUT_FOR_DELIVERY,
                service.updateDeliveryStatus(5L, assignedPartner, OrderStatus.OUT_FOR_DELIVERY).status());
        assertEquals(OrderStatus.DELIVERED,
                service.updateDeliveryStatus(5L, assignedPartner, OrderStatus.DELIVERED).status());
    }

    @Test
    void deliveryPartnerCannotSkipStatusSteps() throws Exception {
        CustomerOrder order = new CustomerOrder(customer(), restaurant(12L, "Overflow Kitchen"),
                "10 Market Street", null);
        AppUser partner = user(80L, "Delivery Partner", UserRole.PARTNER);
        order.assignPartner(partner);
        OrderService service = service(order, List.of(), null);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.updateDeliveryStatus(5L, partner, OrderStatus.DELIVERED));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    private static OrderService service(CustomerOrder storedOrder, List<FoodItem> storedItems,
                                        AppUser assignablePartner) {
        CustomerOrderRepository orders = proxy(CustomerOrderRepository.class, (method, arguments) -> switch (method.getName()) {
            case "save" -> arguments[0];
            case "findById" -> Optional.ofNullable(storedOrder);
            default -> throw new UnsupportedOperationException(method.getName());
        });
        FoodItemRepository foodItems = proxy(FoodItemRepository.class, (method, arguments) -> {
            if (method.getName().equals("findAllById")) return storedItems;
            throw new UnsupportedOperationException(method.getName());
        });
        UserService users = new UserService(null, null) {
            @Override
            public AppUser requirePartner(Long id) {
                if (assignablePartner == null || !assignablePartner.getId().equals(id)) {
                    throw new ApiException(HttpStatus.NOT_FOUND, "Delivery partner not found");
                }
                return assignablePartner;
            }
        };
        return new OrderService(orders, foodItems, users);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, RepositoryHandler handler) {
        InvocationHandler invocationHandler = (instance, method, arguments) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> type.getSimpleName() + " test proxy";
                    case "hashCode" -> System.identityHashCode(instance);
                    case "equals" -> instance == arguments[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                };
            }
            return handler.invoke(method, arguments);
        };
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, invocationHandler);
    }

    private static AppUser customer() throws Exception {
        return user(2L, "Asha Customer", UserRole.CUSTOMER);
    }

    private static AppUser user(Long id, String name, UserRole role) throws Exception {
        AppUser user = new AppUser(name, name.toLowerCase().replace(' ', '.') + "@example.test",
                "hashed-password", null, role);
        setId(user, id);
        return user;
    }

    private static Restaurant restaurant(Long id, String name) throws Exception {
        Restaurant restaurant = new Restaurant(name, "Fresh food", "10 Market Street", null);
        setId(restaurant, id);
        return restaurant;
    }

    private static FoodItem foodItem(Long id, Restaurant restaurant, String name, String price) throws Exception {
        FoodItem item = new FoodItem(restaurant, name, null, new BigDecimal(price), true);
        setId(item, id);
        return item;
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    @FunctionalInterface
    private interface RepositoryHandler {
        Object invoke(java.lang.reflect.Method method, Object[] arguments) throws Throwable;
    }
}
