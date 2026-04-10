# AuctionU - University Auction Platform

Welcome to **AuctionU**, a robust, event-driven microservices architecture designed for a highly scalable university auction platform. It provides a secure space for students to list, bid on, and auction their items within the university community. The system leverages modern Java/Spring Boot practices, reactive caching, and asynchronous messaging.

## 🚀 System Architecture

The system is split into multiple microservices: `authService`, `userService`, and `productService`, decoupled asynchronously via **Apache Kafka** and backed by **Redis** and **MySQL/PostgreSQL**. 

```mermaid
sequenceDiagram
    participant App as Client App
    participant AuthService
    participant Kafka
    participant UserService
    participant ProductService
    participant RedisCache
    participant Database

    App->>AuthService: POST /auth/v1/signup 
    Note right of AuthService: Validates @snu.edu.in domain
    AuthService->>Kafka: Publish "User Created Event"
    AuthService-->>App: 200 OK (JWT Token)

    par Async Profile Creation
        Kafka->>UserService: Consume Event
        UserService->>Database: Provision User Profile
    end

    App->>ProductService: POST /v1/product/auction
    Note over ProductService: Transforms DTO to Entity using MapStruct
    ProductService->>RedisCache: Create Auction Cache (For fast active bidding)
    ProductService->>Database: Save Product Listing
    ProductService->>Kafka: Publish "Auction Created Event"
    ProductService-->>App: 200 OK (Auction Listed)

    App->>ProductService: POST /v1/product/bid
    ProductService->>RedisCache: Validate & Update Highest Bid (Atomic)
    ProductService->>Kafka: Publish "New Bid Event"
    ProductService-->>App: 200 OK (Bid Placed)
```

## 🌟 Key Features

### 1. High Scalability & Event-Driven Operations

The system is designed to handle high-traffic bidding environments effortlessly:

- **Stateless Authentication**: Uses **JWT (JSON Web Tokens)** allowing horizontal scaling of the `authService`. Access is strictly limited to university students (e.g., matching `@snu.edu.in` constraints).
- **Asynchronous Processing**: **Apache Kafka** handles user profile creation and broadcast events for auctions and bids, so no service is blocked on heavy write operations.
- **Blazing Fast Bid Processing**: **Redis** is natively used for fast-paced, real-time bid validation and caching live auction metrics, enabling sub-millisecond read/writes on high-demand items.

### 2. Microservices Overview

#### A. Auth Service (`authService`)
**Responsibility**: Secure authentication, Token Generation, Credential Storage.
- **Endpoints**: Handles student signup, login, and token issuance. Enforces university-domain emails using specialized validation structures (e.g. `UserRegistrationRequest`).
- **Producer**: Publishes events to Kafka upon user signup to inform downstream services. Built with transactional consistency, ensuring successful registration rollbacks if Kafka event parsing fails.
- **Data Integrity**: Uses synchronized UUID database generation and avoids transmitting sensitive credentials like passwords over message buses.

#### B. User Service (`userService`)
**Responsibility**: Managing User Profiles and contact information.
- **Consumer**: Silently watches the global Kafka broker to provision and sync profiles.
- **Integration**: Designed to sync profile mappings tightly with other internal services.

#### C. Product/Auction Service (`productService`)
**Responsibility**: Core listings logistics, placing bids, and matchmaking buyers & sellers.
- **Robust Validation**: Enforces strict backend validation annotations for robust DTO bindings (e.g. tracking constraints for pricing, auction end times, and seller relationships).
- **History Retention**: Enforces soft deletion by transitioning entity statuses to `DELETED` instead of destroying active database rows, thus retaining auction history.
- **Entity Pre-Processing**: Integrates into JPA `@PrePersist` lifecycles to bootstrap entity properties securely upon creation.
- **Real-Time Bidding**: Uses Redis to lock, update, and fetch the highest bids atomically, preventing race conditions during intense last-second bidding wars.
- **Database**: Stores completed auctions and product details robustly in a relational database.

## 🔧 Technical Stack

- **Backend Languages**: Java (Spring Boot) / Kotlin
- **Build Tool**: Gradle
- **Messaging Pipeline**: Apache Kafka
- **Caching & Synchronization**: Redis
- **Database Engine**: Relational (MySQL / PostgreSQL)
- **Security**: Spring Security & JWT, Domain Validation (University emails only)
- **Deployment**: Docker, Docker Compose

## 🏃‍♂️ Local Development Setup

Ensure you have Docker and Docker Compose installed on your system.

1. **Start Backend Infrastructure**:
   Spin up all dependent services (Database, Redis, Kafka server, Zookeeper) along with the microservices.
   ```bash
   docker-compose up -d --build
   ```
2. **Accessing the Services**:
   The services expose internal REST APIs automatically mapped via load balancers downstream. API Keys are configured to securely protect inter-service REST routes.

3. **Stopping the Environment**:
   ```bash
   docker-compose down
   ```
