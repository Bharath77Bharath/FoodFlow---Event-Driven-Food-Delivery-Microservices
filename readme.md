# 🍔 FoodFlow — Event-Driven Food Delivery Microservices

FoodFlow is a production-oriented food delivery backend built using **Java, Spring Boot, PostgreSQL, Apache Kafka, Redis, Docker, Kubernetes, and AWS**.

The project started as a set of basic REST CRUD services and evolved into a **distributed, event-driven microservices architecture** with asynchronous communication, Saga-based distributed transactions, JWT security, service-to-service authentication, resilience patterns, observability, containerization, CI/CD, and cloud deployment.

The goal of FoodFlow is not simply to build a food ordering application, but to demonstrate how a real-world backend can evolve from a monolithic CRUD-style implementation into a **scalable distributed system**.

---

## 🚀 Project Evolution

FoodFlow is being developed in multiple architectural stages:

```text
Basic REST APIs
      ↓
CRUD Microservices
      ↓
Database per Service
      ↓
Service Discovery
      ↓
Synchronous Inter-Service Communication
      ↓
API Gateway
      ↓
Centralized Authentication
      ↓
Authorization & Ownership
      ↓
Apache Kafka
      ↓
Event-Driven Architecture
      ↓
Saga / Distributed Transactions
      ↓
Resilience Patterns
      ↓
Transactional Outbox
      ↓
Redis Caching
      ↓
Observability
      ↓
Docker
      ↓
Automated Testing
      ↓
CI/CD
      ↓
Kubernetes
      ↓
Cloud Deployment
```

---

# 🏗️ Architecture

```
                         ┌────────────────────┐
                         │      Client        │
                         │  Web / Mobile App  │
                         └─────────┬──────────┘
                                   │
                                   ▼
                         ┌────────────────────┐
                         │    API Gateway     │
                         │ Spring Cloud       │
                         │ Gateway MVC         │
                         └─────────┬──────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
                    ▼              ▼              ▼
              ┌──────────┐   ┌───────────┐   ┌───────────┐
              │  User    │   │Restaurant │   │   Order   │
              │ Service  │   │  Service  │   │  Service  │
              └──────────┘   └───────────┘   └─────┬─────┘
                                                    │
                         ┌──────────────────────────┼────────────────────┐
                         │                          │                    │
                         ▼                          ▼                    ▼
                   ┌───────────┐              ┌───────────┐       ┌───────────┐
                   │  Payment  │              │ Inventory │       │  Review   │
                   │  Service  │              │  Service  │       │  Service  │
                   └───────────┘              └───────────┘       └───────────┘
                         │
                         │
                         ▼
                   ┌───────────┐
                   │ Delivery  │
                   │  Service  │
                   └───────────┘

                  ┌──────────────────────────────┐
                  │          Apache Kafka        │
                  │                              │
                  │ order-events                 │
                  │ payment-events               │
                  │ restaurant-events            │
                  │ inventory-events              │
                  │ delivery-events              │
                  │ partner-events               │
                  └──────────────┬───────────────┘
                                 │
                    ┌────────────┴────────────┐
                    ▼                         ▼
             ┌──────────────┐          ┌──────────────┐
             │ Notification │          │  Analytics   │
             │   Service    │          │   Service    │
             └──────────────┘          └──────────────┘

                 ┌──────────────────────┐
                 │ Eureka Service       │
                 │ Registry             │
                 └──────────────────────┘

                 ┌──────────────────────┐
                 │ PostgreSQL           │
                 │ Database per Service │
                 └──────────────────────┘

                 ┌──────────────────────┐
                 │ Redis                │
                 │ Caching / Fast Data  │
                 └──────────────────────┘
```

---

# 🧩 Microservices

## 1. User Service

Responsible for:

- User registration
- Login
- User management
- Profile management
- Address management
- Role management
- JWT generation
- Password hashing
- Admin bootstrap

Supported roles:

```
CUSTOMER
RESTAURANT_OWNER
DELIVERY_PARTNER
ADMIN
```

Security:

- Spring Security
- BCrypt
- JWT
- Role-based authorization
- Resource ownership authorization
- Internal service authentication

---

## 2. Restaurant Service

Responsible for:

- Restaurant management
- Restaurant ownership
- Menu management
- Menu item CRUD
- Restaurant availability
- Restaurant search

Ownership model:

```
Restaurant
    │
    └── ownerId → User Service user
```

Restaurant owners can manage their own restaurants and menus while administrators have broader privileges.

---

## 3. Order Service

The central business service responsible for:

- Creating orders
- Order items
- Order status
- Order history
- Order ownership
- Restaurant ownership validation
- Inventory coordination
- Payment coordination
- Publishing order events

Order lifecycle:

```
PLACED
   ↓
PAYMENT_PROCESSING
   ↓
CONFIRMED
   ↓
PREPARING
   ↓
READY_FOR_PICKUP
   ↓
OUT_FOR_DELIVERY
   ↓
DELIVERED
```

Failure states:

```
PAYMENT_FAILED
CANCELLED
```

---

## 4. Payment Service

Responsible for:

- Payment creation
- Payment processing
- Payment status
- Payment ownership
- Mock payment gateway
- Publishing payment events

Payment events:

```
PAYMENT_CREATED
PAYMENT_SUCCESS
PAYMENT_FAILED
```

The customer initiates payment while the payment service publishes the result asynchronously.

---

## 5. Inventory Service

Responsible for:

- Menu item inventory
- Available quantity
- Reserved quantity
- Inventory reservation
- Inventory release
- Inventory failure handling

Example:

```
Available Quantity
       ↓
Reserve Inventory
       ↓
Reserved Quantity
```

Inventory participates in the order Saga.

---

## 6. Delivery Service

Responsible for:

- Delivery partners
- Delivery partner profiles
- Partner availability
- Delivery assignment
- Delivery lifecycle
- Delivery tracking

Delivery partner states:

```
OFFLINE
AVAILABLE
BUSY
```

Delivery flow:

```
Food Ready
    ↓
Find Available Partner
    ↓
Assign Delivery
    ↓
OUT_FOR_DELIVERY
    ↓
DELIVERED
```

---

## 7. Notification Service

Consumes Kafka events and creates notifications for events such as:

- Order created
- Payment success/failure
- Order confirmed
- Food ready
- Delivery assigned
- Delivery completed

Supported channels:

```
IN_APP
EMAIL
SMS
PUSH
```

Notification Service does not directly modify other service databases.

---

## 8. Review Service

Responsible for:

- Restaurant reviews
- Ratings
- Order-based review validation
- Customer review ownership

Reviews are associated with:

```
User
Restaurant
Order
```

---

## 9. Analytics Service

Designed as an event-driven consumer of business events.

Potential analytics:

- Orders per restaurant
- Revenue
- Payment success/failure rates
- Delivery metrics
- Customer activity
- Restaurant performance

Analytics is intentionally decoupled from transactional services.

---

# 🔍 Service Discovery

FoodFlow uses:

**Eureka Service Registry**

Instead of hardcoding service addresses:

```
http://localhost:8081
http://localhost:8083
...
```

services register themselves with Eureka.

Other services communicate using service names:

```
user-service
restaurant-service
order-service
payment-service
delivery-service
```

This allows service instances to change without changing clients.

---

# 🔄 Inter-Service Communication

FoodFlow uses two communication models.

## Synchronous Communication

Used when an immediate response is required.

Technology:

```
Spring Cloud OpenFeign
```

Example:

```
Order Service
      │
      │ HTTP
      ▼
Restaurant Service
```

---

## Asynchronous Communication

Used for event-driven workflows.

Technology:

```
Apache Kafka
```

Example:

```
Order Service
      │
      │ OrderCreatedEvent
      ▼
    Kafka
      │
      ├───────────────► Payment Service
      │
      ├───────────────► Inventory Service
      │
      └───────────────► Notification Service
```

This reduces tight coupling between services.

---

# 📨 Event-Driven Architecture

FoodFlow uses Kafka as the event backbone.

Major topics include:

```
order-events
payment-events
restaurant-events
inventory-events
delivery-events
partner-events
```

Example:

```
Order Created
      │
      ▼
OrderCreatedEvent
      │
      ▼
Kafka
      │
      ├──► Payment
      ├──► Inventory
      ├──► Notification
      └──► Analytics
```

Services react to events independently.

---

# 🔁 Saga Pattern

FoodFlow uses the **Saga pattern** to coordinate distributed transactions.

An order touches multiple services:

```
Order
  ↓
Payment
  ↓
Inventory
  ↓
Restaurant
  ↓
Delivery
```

There is no single database transaction across all services.

Instead, each service performs its own local transaction and publishes events.

Example:

```
Order Created
      ↓
Payment Processing
      ↓
Payment Success
      ↓
Inventory Reservation
      ↓
Inventory Reserved
      ↓
Order Confirmed
      ↓
Restaurant Accepts
      ↓
Food Ready
      ↓
Delivery Assigned
```

Failure events trigger compensating actions where required.

---

# 🔐 Security

FoodFlow uses Spring Security and JWT.

Authentication flow:

```
Client
  │
  ▼
User Service
  │
  ├── BCrypt password verification
  │
  └── JWT generation
          │
          ▼
     API Gateway
          │
          ▼
      Microservices
```

JWT contains:

```
sub
role
email
iat
exp
```

Services validate the JWT independently.

---

# 🛡️ Authorization

Authorization is implemented at the service level.

Two major concepts are used:

### Role-Based Access Control

Examples:

```
CUSTOMER
RESTAURANT_OWNER
DELIVERY_PARTNER
ADMIN
```

Implemented using:

```
@PreAuthorize(...)
```

### Resource Ownership

Examples:

```
Customer → own orders
Restaurant Owner → own restaurants
Restaurant Owner → own menu items
Delivery Partner → own profile
Customer → own payments
```

This prevents users from accessing another user's resources even when they have the correct general role.

---

# 🔑 Internal Service Authentication

Kafka consumers do not have a user's JWT.

Therefore, internal HTTP calls originating from asynchronous processing use a separate service credential.

Example:

```
Delivery Service
      │
      │ X-Internal-Service-Key
      ▼
User Service
```

The target service recognizes:

```
ROLE_SERVICE
```

This separates:

```
User Identity
```

from:

```
Service Identity
```

---

# 🧱 Design Patterns

FoodFlow demonstrates multiple backend and distributed-system patterns.

### Microservices Architecture

Each business domain owns its service and database.

### Database-per-Service

Each service owns its own PostgreSQL database.

```
User DB
Restaurant DB
Order DB
Payment DB
Delivery DB
Inventory DB
Review DB
Notification DB
```

Services do not directly access another service's database.

### API Gateway Pattern

All external requests enter through the API Gateway.

### Service Discovery Pattern

Eureka provides service registration and discovery.

### Saga Pattern

Coordinates distributed business transactions.

### Event-Driven Architecture

Kafka events decouple business workflows.

### Publish/Subscribe

Multiple services can consume the same business event independently.

### Repository Pattern

Spring Data repositories abstract database access.

### DTO Pattern

Request and response DTOs prevent exposing persistence entities directly.

### Strategy Pattern

Used where different processing strategies can be introduced without changing the core workflow.

### Adapter Pattern

External/mock integrations are isolated behind service interfaces.

### Interceptor Pattern

Feign interceptors propagate authentication credentials and internal service credentials.

### Builder / Factory-style Construction

Used where object creation becomes complex and domain-specific.

---

# ⚡ Resilience

The project includes a dedicated reliability phase.

Planned/implemented resilience mechanisms include:

- Resilience4j
- Circuit Breaker
- Retry
- Timeout
- Rate Limiting
- Bulkhead
- Fallback handling

Example:

```
Order Service
      │
      ▼
Restaurant Service
      │
   failure
      ▼
Circuit Breaker
      │
      ├── Retry
      ├── Timeout
      └── Fallback
```

The objective is to prevent a failure in one service from cascading through the entire system.

---

# 📦 Transactional Outbox

The architecture includes a transactional outbox phase to improve event reliability.

Without an outbox:

```
DB Transaction
      │
      ├── Save Order
      │
      └── Publish Kafka Event
```

A failure between these operations can create inconsistency.

With the Outbox pattern:

```
Database Transaction
      │
      ├── Save Order
      │
      └── Save Outbox Event
                │
                ▼
          Outbox Publisher
                │
                ▼
              Kafka
```

This provides a reliable bridge between database transactions and Kafka events.

---

# ⚡ Redis

Redis is introduced for high-speed data access and caching.

Potential use cases include:

- Restaurant data caching
- Menu caching
- Frequently accessed data
- Location-related data
- Temporary state
- Reducing database load

Architecture:

```
Client
  ↓
Service
  ↓
Redis
  │
  └── cache miss
        ↓
     PostgreSQL
```

---

# 📊 Observability

FoodFlow includes an observability layer using:

```
Spring Boot Actuator
Micrometer
Prometheus
Grafana
OpenTelemetry
```

Observability covers:

- Application health
- Metrics
- Request latency
- Error rates
- Service health
- Kafka metrics
- Distributed tracing

Example:

```
Microservices
     │
     ▼
  Micrometer
     │
     ▼
 Prometheus
     │
     ▼
  Grafana
```

Distributed tracing is designed to follow a request across:

```
Gateway
   ↓
Order
   ↓
Payment
   ↓
Inventory
   ↓
Delivery
```

---

# 🐳 Docker

Each microservice is containerized.

Example:

```
foodflow/
├── user-service
├── restaurant-service
├── order-service
├── payment-service
├── inventory-service
├── delivery-service
├── notification-service
├── review-service
├── analytics-service
├── api-gateway
└── service-registry
```

Docker Compose is used to run infrastructure locally.

Infrastructure includes:

```
PostgreSQL
Kafka
Zookeeper / Kafka dependencies
Redis
Prometheus
Grafana
```

---

# ☁️ Cloud & DevOps

The project is designed to move from local development toward cloud deployment.

Cloud/DevOps technologies:

```
AWS
Docker
GitHub Actions
CI/CD
Terraform
Kubernetes
```

Planned deployment architecture:

```
                   Internet
                       │
                       ▼
                 Load Balancer
                       │
                       ▼
                API Gateway
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
     Services       Services       Services
        │              │              │
        └──────────────┼──────────────┘
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
       Kafka         Redis       PostgreSQL
```

---

# 🔄 CI/CD

GitHub Actions is used to automate:

```
Git Push
   ↓
Build
   ↓
Unit Tests
   ↓
Integration Tests
   ↓
Docker Build
   ↓
Docker Image
   ↓
Deployment
```

The objective is to move toward a fully automated delivery pipeline.

---

# ☸️ Kubernetes

The final deployment stage targets Kubernetes.

Kubernetes responsibilities include:

- Container orchestration
- Service discovery
- Scaling
- Self-healing
- Rolling deployments
- Configuration management
- Secret management

Example:

```
Kubernetes Cluster
│
├── API Gateway
├── User Service
├── Restaurant Service
├── Order Service
├── Payment Service
├── Inventory Service
├── Delivery Service
├── Notification Service
├── Review Service
└── Analytics Service
```

---

# 🧪 Testing

Testing is part of the development lifecycle.

Planned testing layers:

```
Unit Tests
    ↓
Integration Tests
    ↓
Repository Tests
    ↓
Controller Tests
    ↓
Kafka Tests
    ↓
End-to-End Tests
```

The objective is to validate both individual services and distributed workflows.

---

# 🛠️ Tech Stack

CategoryTechnologyLanguageJava 21FrameworkSpring BootSecuritySpring SecurityAuthenticationJWTAPI GatewaySpring Cloud Gateway MVCService DiscoveryEurekaSync CommunicationSpring Cloud OpenFeignAsync CommunicationApache KafkaDatabasePostgreSQLCacheRedisORMSpring Data JPA / HibernateValidationJakarta ValidationResilienceResilience4jMetricsMicrometerMonitoringPrometheusDashboardsGrafanaTracingOpenTelemetryContainerizationDockerOrchestrationKubernetesCI/CDGitHub ActionsInfrastructureTerraformCloudAWS

---

# 📁 High-Level Project Structure

```
FoodFlow/
│
├── api-gateway/
├── service-registry/
│
├── user-service/
├── restaurant-service/
├── order-service/
├── payment-service/
├── inventory-service/
├── delivery-service/
├── notification-service/
├── review-service/
└── analytics-service/
```

---

# 🔀 Order Processing Example

A complete order flow demonstrates the distributed architecture:

```
Customer
   │
   ▼
API Gateway
   │
   ▼
Order Service
   │
   └── OrderCreatedEvent
          │
          ▼
        Kafka
          │
          ├──────────────► Payment Service
          │                     │
          │                     └── PaymentSuccessEvent
          │
          ├──────────────► Inventory Service
          │                     │
          │                     └── InventoryReservedEvent
          │
          └──────────────► Notification Service

Payment Success
        +
Inventory Reserved
        │
        ▼
Order Confirmed
        │
        ▼
Restaurant Processing
        │
        ▼
Food Ready
        │
        ▼
Delivery Service
        │
        ▼
Delivery Partner Assigned
        │
        ▼
Out For Delivery
        │
        ▼
Delivered
        │
        ▼
Notification + Analytics
```

---

# 🎯 Engineering Concepts Demonstrated

FoodFlow is designed to demonstrate practical understanding of:

- REST API development
- CRUD operations
- Domain-driven service boundaries
- Microservices architecture
- Database-per-service
- Service discovery
- API Gateway
- Synchronous communication
- Asynchronous communication
- Event-driven architecture
- Kafka producers and consumers
- Distributed transactions
- Saga pattern
- Event choreography
- Authentication
- Authorization
- RBAC
- Resource ownership
- Service-to-service authentication
- Caching
- Resilience
- Circuit breakers
- Transactional Outbox
- Distributed tracing
- Metrics and monitoring
- Containerization
- CI/CD
- Infrastructure as Code
- Kubernetes
- Cloud deployment

---

# 📌 Current Status

FoodFlow is being developed incrementally.

### Completed

- User Service
- Restaurant Service
- Order Service
- Payment Service
- Delivery Service
- Inventory Service
- Review Service
- Notification Service
- Eureka Service Discovery
- API Gateway
- JWT Authentication
- Role-based Authorization
- Resource Ownership Authorization
- OpenFeign communication
- Apache Kafka integration
- Event-driven order workflow
- Saga-based inventory coordination
- Internal service authentication

### In Progress

- Authorization hardening
- Resilience4j
- Transactional Outbox
- Redis
- Observability
- Testing
- Docker
- CI/CD
- Kubernetes
- Analytics

---

# 👨‍💻 Project Focus

The primary focus of FoodFlow is understanding **why distributed systems are designed the way they are**, rather than simply implementing individual APIs.

The project explores the trade-offs between:

```
Synchronous vs Asynchronous Communication
Database Transactions vs Distributed Transactions
REST vs Events
Coupling vs Decoupling
Consistency vs Availability
Latency vs Reliability
Local Development vs Cloud Deployment
```

---

# 📚 Learning Objective

FoodFlow serves as a hands-on implementation of modern backend engineering and distributed systems concepts using the Spring ecosystem.

The project progressively introduces complexity so that every architectural decision builds on the previous stage.

```
CRUD
 ↓
Microservices
 ↓
Distributed Communication
 ↓
Security
 ↓
Events
 ↓
Distributed Transactions
 ↓
Reliability
 ↓
Observability
 ↓
Containers
 ↓
CI/CD
 ↓
Cloud-Native Architecture
```

---

# 👨‍💻 Author

**Bharath Lenin**

Aspiring Software Developer | Backend Engineering | Cloud & DevOps Enthusiast

GitHub: https://github.com/77Bharath
