# FoodFlow Frontend

A functional, lightweight React + Vite frontend for demonstrating and interacting with the **FoodFlow** microservices food-delivery ecosystem.

---

## 🚀 Quick Start

### 1. Prerequisites
- Node.js (v18+ or v20+)
- npm (v9+ or v10+)
- FoodFlow Backend running via Docker Compose or locally (API Gateway on port 8080)

### 2. Installation
Navigate into the `Frontend` directory and install dependencies:
```bash
npm install
```

### 3. Environment Configuration
Copy `.env.example` to `.env` (or customize as needed):
```bash
cp .env.example .env
```
Default `.env` configuration:
```env
VITE_API_BASE_URL=http://localhost:8080
```
- **Local Dev Server Proxy**: By default, `vite.config.js` also configures a reverse proxy from `/api` to `http://localhost:8080`.
- All requests target the **API Gateway** (`http://localhost:8080`), preserving the microservice gateway architecture.

### 4. Running the Development Server
```bash
npm run dev
```
The application will launch at:
```
http://localhost:5173
```

### 5. Building for Production
```bash
npm run build
```
Generates production-ready static assets in `dist/`.

---

## 🐳 Docker Support

A `Dockerfile` is included in `Frontend/` for containerized deployments:
```bash
docker build -t foodflow-frontend .
docker run -p 80:80 foodflow-frontend
```

---

## 👥 Supported Roles & Default Flow

FoodFlow uses JWT-based authentication with role-based access control.

| Role | Available Features & Flows |
|---|---|
| **CUSTOMER** | Browse restaurants, search by city/name, view menus, add items to Cart, place orders, execute payment (UPI/Card/Cash), track live order status timeline, view order notifications, and submit reviews. |
| **RESTAURANT_OWNER** | Register new restaurants, view/update restaurant info, deactivate restaurants, add/edit/delete menu items, and manage menu item inventory levels. |
| **DELIVERY_PARTNER** | Register delivery partner vehicle profile, toggle availability status (`AVAILABLE` vs `OFFLINE`), lookup assigned deliveries, and transition delivery status (`PICKED_UP`, `OUT_FOR_DELIVERY`, `DELIVERED`). |
| **ADMIN** | Full administrative console: User management (list/delete users), delivery partners (status toggle/delete), all delivery logs, inventory oversight, and manual order status override. |

---

## 🔌 Integrated Microservices APIs

All calls are routed through the **API Gateway** (`http://localhost:8080`):

| Service | Endpoint Pattern | Description |
|---|---|---|
| **User Service** | `POST /api/v1/auth/login`<br>`POST /api/v1/auth/register`<br>`GET /api/v1/users/{id}`<br>`GET /api/v1/users` *(Admin)*<br>`PUT /api/v1/users/{id}`<br>`DELETE /api/v1/users/{id}` | Authentication, registration, and user profiles |
| **Restaurant Service** | `GET /api/v1/restaurants`<br>`GET /api/v1/restaurants/{id}`<br>`GET /api/v1/restaurants/{id}/availability`<br>`GET /api/v1/restaurants/search/city`<br>`GET /api/v1/restaurants/search/name`<br>`POST /api/v1/restaurants`<br>`PUT /api/v1/restaurants/{id}`<br>`DELETE /api/v1/restaurants/{id}`<br>`GET /api/v1/restaurants/{id}/menu-items`<br>`POST /api/v1/restaurants/{id}/menu-items`<br>`GET /api/v1/menu-items/{id}`<br>`PUT /api/v1/menu-items/{id}`<br>`DELETE /api/v1/menu-items/{id}` | Restaurant listing, search, details, and menu item CRUD |
| **Order Service** | `POST /api/v1/order`<br>`GET /api/v1/order/{id}`<br>`GET /api/v1/order/user/{userId}`<br>`GET /api/v1/order/{id}/status`<br>`PATCH /api/v1/order/{id}/status` *(Admin)* | Order creation, retrieval by user/id, status tracking, and status overrides |
| **Payment Service** | `POST /api/v1/payments`<br>`POST /api/v1/payments/{id}/process`<br>`GET /api/v1/payments/{id}`<br>`GET /api/v1/payments/order/{orderId}` | Payment initiation (UPI, CARD, CASH), processing, and verification |
| **Delivery Service** | `POST /api/v1/delivery-partners`<br>`GET /api/v1/delivery-partners/{id}`<br>`GET /api/v1/delivery-partners` *(Admin)*<br>`PUT /api/v1/delivery-partners/my-status`<br>`PUT /api/v1/delivery-partners/{id}/status`<br>`GET /api/v1/deliveries/order/{orderId}`<br>`GET /api/v1/deliveries/{id}`<br>`GET /api/v1/deliveries` *(Admin)*<br>`PUT /api/v1/deliveries/{id}/status` | Delivery partner profile, availability toggle, delivery assignments, and status updates |
| **Inventory Service** | `POST /api/v1/inventory`<br>`GET /api/v1/inventory/{id}`<br>`GET /api/v1/inventory/menu-item/{menuItemId}`<br>`GET /api/v1/inventory` *(Admin)*<br>`PUT /api/v1/inventory/{id}`<br>`DELETE /api/v1/inventory/{id}`<br>`POST /api/v1/inventory/check-availability` | Menu item stock tracking, quantity updates, and availability verification |
| **Review Service** | `POST /api/v1/review`<br>`GET /api/v1/review/{id}`<br>`GET /api/v1/review/restaurants/{restaurantId}`<br>`GET /api/v1/review/users/{userId}`<br>`PUT /api/v1/review/{id}`<br>`DELETE /api/v1/review/{id}` | Rating and comment submissions for delivered orders and restaurant review listings |
| **Notification Service** | `GET /api/v1/notification/{id}`<br>`GET /api/v1/notification/users/{userId}`<br>`GET /api/v1/notification/orders/{orderId}` | Event notifications for user orders and status updates |

---

## 🔐 Authentication & Session Handling

- The frontend automatically stores the token (`foodflow_token`) and user object (`foodflow_user`) in `localStorage` upon login.
- An Axios interceptor automatically attaches `Authorization: Bearer <token>` to all authenticated requests.
- Automatic handling for `401 Unauthorized` (clears invalid session, redirects to `/login`) and `403 Forbidden` (in-app permission notifications).
