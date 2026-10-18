# Project Overflow

Project Overflow is a food-delivery REST API built with Java 25, Spring Boot 4.1.1, Maven, Spring Data JPA, Spring Security, and MySQL.

## Current MVP

- Customers can register, browse active restaurants and menus, place orders, and view their own orders.
- Admins can create, edit, and deactivate restaurants and food items; view all orders; and assign orders to delivery partners.
- Delivery partners can view their assignments and advance delivery status through `PICKED_UP`, `OUT_FOR_DELIVERY`, and `DELIVERED`.
- Passwords are stored as BCrypt hashes. Public customer registration always creates a `CUSTOMER`; it cannot grant admin or partner access.
- Restaurant and food-item deletion is soft deletion so historical orders remain readable.

## Run locally

1. Create a MySQL schema named `overflow_db` and a local account with access to it.
2. Set `DB_PASSWORD` in IntelliJ's **Run → Edit Configurations → Environment variables**. The committed `application.properties` contains a placeholder and no database password.
3. To seed the first administrator, also set `OVERFLOW_ADMIN_NAME`, `OVERFLOW_ADMIN_EMAIL`, and `OVERFLOW_ADMIN_PASSWORD` in that local run configuration. The account is created only when all three values are present and that email does not already exist. Keep the password private. You may remove these three variables after the first successful startup.
4. Run `OverflowApplication` from IntelliJ or run `mvn spring-boot:run` from the project directory.

The app listens on `http://localhost:8080`. Hibernate creates or updates the local schema. This is convenient for the MVP; use versioned database migrations before production deployment.

## Authentication

The API uses HTTP Basic authentication for this local MVP. In Postman, choose **Authorization → Basic Auth**, then enter the account email and password. Do not use plain HTTP Basic over a public network; deployment needs HTTPS. Registration is public, but all customer order operations require a customer account.

## API overview

### Public catalog

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/restaurants` | Browse active restaurants |
| `GET` | `/api/restaurants/{restaurantId}` | View one active restaurant |
| `GET` | `/api/restaurants/{restaurantId}/items` | Browse available menu items |
| `POST` | `/api/auth/register` | Register a customer |

Example customer registration:

```json
{
  "name": "Asha Customer",
  "email": "asha@example.com",
  "password": "<choose a private password with at least 8 characters>",
  "phone": "555-0100"
}
```

### Admin (HTTP Basic account with `ADMIN` role)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/admin/restaurants` | List all restaurants, including inactive ones |
| `POST` | `/api/admin/restaurants` | Add a restaurant |
| `PUT` | `/api/admin/restaurants/{id}` | Edit a restaurant |
| `DELETE` | `/api/admin/restaurants/{id}` | Deactivate a restaurant |
| `GET` | `/api/admin/restaurants/{restaurantId}/items` | List all menu items, including unavailable ones |
| `POST` | `/api/admin/restaurants/{restaurantId}/items` | Add a food item |
| `PUT` | `/api/admin/items/{id}` | Edit a food item |
| `DELETE` | `/api/admin/items/{id}` | Mark a food item unavailable |
| `POST` | `/api/admin/partners` | Create a delivery partner account |
| `GET` | `/api/admin/partners` | List active delivery partners |
| `GET` | `/api/admin/orders` | View all orders |
| `PUT` | `/api/admin/orders/{orderId}/assignment` | Assign/reassign a delivery partner |

Restaurant request:

```json
{
  "name": "Overflow Kitchen",
  "description": "Fresh meals made to order",
  "address": "10 Market Street",
  "phone": "555-0101"
}
```

Food item request:

```json
{
  "name": "Vegetable Rice Bowl",
  "description": "Rice, vegetables, and house sauce",
  "price": 8.50,
  "available": true
}
```

Create a partner with `POST /api/admin/partners` using `name`, `email`, `password`, and optional `phone`. Assign an order by sending `{"partnerId": 2}` to the assignment endpoint.

### Customer (HTTP Basic account with `CUSTOMER` role)

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/orders` | Place an order |
| `GET` | `/api/orders/mine` | List the signed-in customer's orders |
| `GET` | `/api/orders/{id}` | View an owned order |

Order request:

```json
{
  "deliveryAddress": "42 Example Avenue",
  "notes": "Ring the bell",
  "items": [
    { "foodItemId": 1, "quantity": 2 }
  ]
}
```

An order can contain items from one restaurant only. The server reads prices from the menu and stores a price/name snapshot with each order item.

### Delivery partner (HTTP Basic account with `PARTNER` role)

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/partner/deliveries` | View this partner's assigned deliveries |
| `PATCH` | `/api/partner/deliveries/{orderId}/status` | Advance an assigned delivery |

Status updates must follow `ASSIGNED → PICKED_UP → OUT_FOR_DELIVERY → DELIVERED`. A partner cannot update another partner's delivery.

## End-to-end walkthrough

Use Postman with the base URL http://localhost:8080. For protected requests, choose **Authorization → Basic Auth** and use the account for the role listed below. Substitute the IDs returned by earlier requests.

1. **Admin:** Create a restaurant with **POST /api/admin/restaurants**, using the restaurant request example above. Save its id.
2. **Admin:** Add a food item with **POST /api/admin/restaurants/{restaurantId}/items**, using the food item example above. Save its id.
3. **Admin:** Create a delivery partner with **POST /api/admin/partners**. Save the partner's email and password for partner requests.
4. **Customer:** Register with **POST /api/auth/register**, then use that email and password for customer requests.
5. **Public/customer:** Browse **GET /api/restaurants/{restaurantId}/items** and confirm the food item is listed.
6. **Customer:** Place an order with **POST /api/orders**, using the saved food item ID and a delivery address. Save the returned order id.
7. **Admin:** Assign the order with **PUT /api/admin/orders/{orderId}/assignment** and body {"partnerId": 1} (replace 1 with the created partner's ID).
8. **Partner:** View assignments with **GET /api/partner/deliveries**. Advance the assigned order with **PATCH /api/partner/deliveries/{orderId}/status**, sending these bodies in order:
   - {"status":"PICKED_UP"}
   - {"status":"OUT_FOR_DELIVERY"}
   - {"status":"DELIVERED"}
9. **Customer:** Check **GET /api/orders/mine** and confirm the order status is DELIVERED.

## MVP boundaries

This project currently has no payment processing, customer cancellation, delivery fee/tax calculation, image uploads, JWT/refresh-token flow, pagination, or frontend. HTTP Basic is intended for local API practice; production deployment needs HTTPS, managed secrets, database migrations, and additional operational controls.

