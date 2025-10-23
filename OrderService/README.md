# OrderService

Simple Spring Boot 3 application implementing core order management features:
- Create an order with multiple items
- Retrieve order details by ID
- Update order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED*)
- List all orders (optionally filtered by status)
- Cancel an order (only if still PENDING)
- Background job promotes all PENDING orders to PROCESSING every 5 minutes
- Return computed order total (sum of quantity * price per item)

> *CANCELLED was added (assumption) to retain a record of cancelled orders instead of deleting them.

## Tech Stack
- Java 17
- Spring Boot (Web, Data JPA, Validation, Scheduling)
- MySQL (production/dev) / H2 (tests)
- Maven

## Prerequisites
- Java 17 installed
- Maven wrapper (included) or Maven 3.9+
- MySQL running on `localhost:3307` (adjust in `src/main/resources/application.properties` if needed)

Create the database automatically (enabled via `createDatabaseIfNotExist=true`). MySQL user/password currently set to `root` / `singcontroller` — change as appropriate.

## Running the Application
```bash
./mvnw spring-boot:run
```
App starts on port `8083`.

## API Endpoints
Base path: `/api/orders`

1. Create Order
   - `POST /api/orders`
   - Body:
```json
{
  "customerName": "Alice",
  "items": [
    { "name": "Widget", "quantity": 2, "price": 9.99 },
    { "name": "Gadget", "quantity": 1, "price": 19.50 }
  ]
}
```
   - Response `201 Created` (example):
```json
{
  "id": 1,
  "customerName": "Alice",
  "status": "PENDING",
  "createdAt": "2025-10-23T15:42:01.123Z",
  "total": 39.48,
  "items": [
    { "id": 1, "name": "Widget", "quantity": 2, "price": 9.99 },
    { "id": 2, "name": "Gadget", "quantity": 1, "price": 19.5 }
  ]
}
```

2. Get Order by ID
   - `GET /api/orders/{id}`

3. List Orders (optional status filter)
   - `GET /api/orders`
   - `GET /api/orders?status=PROCESSING`

4. Update Status
   - `PUT /api/orders/{id}/status?status=SHIPPED`
   - Allowed statuses: `PENDING, PROCESSING, SHIPPED, DELIVERED` (cannot change if already CANCELLED)

5. Cancel Order
   - `POST /api/orders/{id}/cancel`
   - Only if current status is `PENDING`.

## Sample curl Commands
```bash
# Create an order
curl -s -X POST http://localhost:8083/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerName":"Bob","items":[{"name":"Widget","quantity":3,"price":5.5}]}' | jq .

# Get order 1
curl -s http://localhost:8083/api/orders/1 | jq .

# List all orders
curl -s http://localhost:8083/api/orders | jq .

# List processing orders
curl -s http://localhost:8083/api/orders?status=PROCESSING | jq .

# Update status to SHIPPED
curl -s -X PUT "http://localhost:8083/api/orders/1/status?status=SHIPPED" | jq .

# Cancel an order (only if PENDING)
curl -s -X POST http://localhost:8083/api/orders/1/cancel | jq .
```

## Scheduled Task
A scheduler (`PendingOrderScheduler`) runs every 5 minutes (`fixedRate=300000`) promoting all `PENDING` orders to `PROCESSING`.

## Validation & Errors
- Request bodies validated (customer name non-blank, items non-empty, quantity & price positive).
- Errors returned as JSON with field messages.
- Domain errors (not found, invalid state) returned with `error` field.

## Tests
- Unit-like JPA tests (`OrderServiceTests`) run against H2 with profile `test`.
- Integration tests (`OrderControllerIntegrationTests`) exercise the REST API.

Run tests:
```bash
./mvnw test
```

## Extensibility Ideas (Next Steps)
- Enforce forward-only status transition rules.
- Pagination & sorting for list endpoint.
- Add optimistic locking to prevent concurrent updates.
- Externalize credentials (environment variables / Vault).
- Add item-level discounts or tax calculations.

## Notes
If you do not want CANCELLED status, remove it from the `OrderStatus` enum and adjust cancel logic to delete the order instead.

## License
Internal / Example usage.
