# AuctionU - University Marketplace Platform

Welcome to **AuctionU**, a robust, event-driven microservices architecture designed alongside a modern, dynamic Next.js frontend to securely facilitate university marketplace trades. It provides a secure space for students to list items, participate in live bidding, and manage transactions within the campus community. The system leverages modern Java/Spring Boot microservices, Next.js App Router UI, reactive caching, real-time WebSocket broadcasting, distributed lock scheduling, asynchronous messaging, and transactional email notifications.

## 🚀 System Architecture

The platform spans a full-stack environment consisting of a **Next.js frontend (`auction-ui`)** seamlessly wired to a scalable Spring Boot backend consisting of multiple microservices: `gateway`, `authService`, `userService`, `productService`, and `notificationService`. Services are decoupled via **Apache Kafka** using the **Transactional Outbox Pattern**, and backed by **Redis** and **MySQL**. All internal services are discovered automatically via **Eureka**.

```mermaid
sequenceDiagram
    participant UI as Next.js Frontend
    participant Gateway as API Gateway
    participant AuthService
    participant Kafka
    participant UserService
    participant ProductService as ProductService
    participant OutboxProcessor
    participant NotificationService
    participant MailServer as SMTP Server
    participant Redis
    participant Database as MySQL Cluster

    UI->>Gateway: POST /auth/v1/signup
    Gateway->>AuthService: Route Request
    AuthService->>Redis: Store Temporary User & OTP
    AuthService->>Kafka: Publish "otp-events"
    AuthService-->>Gateway: 200 OK (OTP Sent)
    Gateway-->>UI: 200 OK (OTP Sent)

    par Async OTP Email
        Kafka->>NotificationService: Consume "otp-events"
        NotificationService->>MailServer: Send OTP Email
    end

    UI->>Gateway: POST /auth/v1/verify-signup
    Gateway->>AuthService: Route Request
    AuthService->>Redis: Validate OTP
    AuthService->>Kafka: Publish "userEvents" (User Created)
    AuthService-->>Gateway: 200 OK (Account Created)
    Gateway-->>UI: 200 OK (Account Created)

    UI->>Gateway: POST /auth/v1/login
    Gateway->>AuthService: Route Request
    AuthService-->>Gateway: 200 OK (JWT + Refresh Token)
    Gateway-->>UI: 200 OK (JWT + Refresh Token)

    par Async Profile Creation
        Kafka->>UserService: Consume Event
        UserService->>Database: Provision User Profile
    end

    UI->>Gateway: POST /api/v1/products/create
    Gateway->>ProductService: Route to ProductService (with X-User-Id header)
    Note over ProductService: Transforms DTO to Entity using MapStruct
    ProductService->>Database: Save Product Listing

    UI->>Gateway: POST /api/v1/products/{id}/bid
    Gateway->>ProductService: Route Request
    ProductService->>Database: Update Current Highest Bid (Atomic Tx)
    ProductService->>Database: Save BID_PLACED Event to Outbox Table
    ProductService->>Redis: Publish "productId:New bid: amount" to auction-updates channel
    Redis-->>UI: WebSocket push via STOMP /topic/product/{productId}

    loop Every 5 seconds
        OutboxProcessor->>Database: Poll unprocessed outbox entries
        OutboxProcessor->>Kafka: Publish "product-events" topic
        OutboxProcessor->>Database: Mark entry as processed
    end

    loop Every 60 seconds (ShedLock protected)
        Note over ProductService: AuctionReaper scheduled job
        ProductService->>Database: Find expired ACTIVE auctions
        ProductService->>Database: Mark SOLD (winner) or INACTIVE (no bids)
        ProductService->>Database: Write AUCTION_CLOSED / AUCTION_EXPIRED to Outbox
    end

    par Async Email Notifications
        Kafka->>NotificationService: Consume "product-events"
        NotificationService->>UserService: Feign GET /api/v1/users/{userId} (Eureka-resolved)
        UserService-->>NotificationService: UserResponseDto (name, email)
        NotificationService->>MailServer: Send transactional email via SMTP
    end
```

## 🌟 Key Features

### 1. Full-Stack End-to-End Environment

Everything from the sophisticated **TailwindCSS Next.js UI** down to the **Redis WebSocket STOMP relays** is containerized. It works flawlessly under **Docker Compose**, wrapping 11 integrated clusters natively.

### 2. High Scalability & Event-Driven Operations

The system is engineered to handle high-traffic bidding environments effortlessly:

- **Stateless Authentication**: Uses **JWT (JSON Web Tokens)** allowing horizontal scaling of the `authService`.JWT claims embed the `userId` natively, securely translating to the `X-User-Id` header through the API Gateway footprint.
- **Transactional Outbox Pattern**: Instead of publishing directly to Kafka (risking failure), domain events (`BID_PLACED`, `PURCHASE`, `AUCTION_CLOSED`) are saved atomically to the DB's `outbox` table. A separate polling `OutboxProcessor` guarantees at-least-once message delivery via Kafka.
- **Distributed Lock Scheduling (ShedLock)**: The automated `AuctionReaper` job uses ShedLock (`shedlock-provider-jdbc-template`) synchronized on MySQL. This completely eliminates race conditions and duplicate auction settlements when scaling instances horizontally.

### 3. Real-Time Bid Broadcasting (WebSocket + Redis Pub/Sub)

Instant visual updates to users using STOMP WebSocket communication dynamically bridging to the UI dashboard:

1. `ProductService` publishes `"{productId}:New bid: {amount}"` to the **Redis `auction-updates` channel**.
2. **`RedisSubscriber`** detects and resolves it over STOMP topic `/topic/product/{productId}` via `SimpMessagingTemplate`.
3. The **Next.js UI (`use-auction-socket.ts`)** immediately updates the React state reflecting live numbers continuously without requiring HTTP refresh loops.

### 4. Microservices Overview

- **A. UI Frontend (`auction-ui`)**: Next.js App Router featuring responsive Shadcn-UI design, Framer Motion animations, comprehensive Auth caching logic, and `stompjs/sockjs-client` socket channels for live auction interactions.
- **B. API Gateway (`gateway`)**: Centralized security, rate limiting, token parsing, and service routing. Netty WebFlux server capable of translating CORS correctly and parsing tokens.
- **C. Auth Service (`authService`)**: Secure enrollment with complex password constraints, OTP-based email verification, and JWT issuer.
- **D. User Service (`userService`)**: Independent persistence of user profiles communicating through shared secrets restricting any external endpoint hijacking.
- **E. Product Service (`productService`)**: Core logistics engine governing the business rules behind `AUCTION` and `FIXED_PRICE` sales types.
- **F. Notification Service (`notificationService`)**: Spring Boot Mail / Thymeleaf engine listening on consumers (`otp-events`, `product-events`) and sending emails via SMTP using Eureka + OpenFeign. This service is intentionally stateless in the current implementation (no JPA entities/repositories), so the `notificationservice` MySQL schema exists but has no application tables.

## 🔧 Technical Stack

- **Frontend**: Next.js 14 (App Router), TypeScript, Tailwind CSS, Framer Motion, Zustand/Context, Stompjs
- **Backend Core**: Java 17, Spring Boot 3.x, Gradle 8.x
- **Infrastructure**: Spring Cloud Gateway, Netflix Eureka Server
- **Messaging Layer**: Apache Kafka, Zookeeper, Redis Pub/Sub
- **Databases**: MySQL (JPA/Hibernate), JDBC Template
- **DevOps**: Docker, Docker Compose, Flyway/Init SQLs
- **Protocols**: WebSocket (STOMP)/SockJS, REST, JWT Authentication

## 🏃‍♂️ Running the Project (Docker Compose)

The entire environment including the databases, message brokers, caching mechanisms, frontend, and backend JVM instances are bundled natively. Simply run:

Before running, create a local `.env` file (never commit it) from `.env.example` and set:

```bash
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your-app-password
```

```bash
docker-compose up --build -d
```

### What happens?

1. The `mysql:8.0` cluster boots up and initializes service schemas via `init.sql`; only services that persist domain state create tables (for example, product service tables such as `shedlock`), while `notificationService` currently keeps no DB tables by design.
2. Infrastructure shards `zookeeper`, `kafka`, `eureka-server`, and `redis-alpine` boot alongside one another.
3. Every individual Spring Boot service successfully compiles inside multi-stage `eclipse-temurin` pipelines and starts, connecting and automatically registering their domains natively to the `eureka` discovery pipeline.
4. Finally, the **Next.js UI container** builds and spins up at port `3000`.

### Access Matrices

- **Frontend Dashboard**: `http://localhost:3000` (Visit this visually to view the platform)
- **API Gateway Base**: `http://localhost:8080/` (REST communication footprint)
- **Discovery Console**: `http://localhost:8761/` (Eureka Load Balancer console tracking node health statuses)

### Stopping

```bash
docker-compose down
```
