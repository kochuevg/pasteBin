# Pastebin Clone

A scalable, RESTful backend architecture designed to handle high-throughput read traffic and large text payloads efficiently. 

Unlike standard CRUD applications, this project was engineered with a heavy focus on database protection, memory-efficient analytics, and eventual consistency to survive sudden viral traffic spikes.

## Tech Stack
* Core: Java 21, Spring Boot 4, Spring Security (JWT)
* Database: PostgreSQL (Relational metadata)
* Cache & Analytics: Redis (LFU Caching, HyperLogLog)
* Object Storage: MinIO (S3-Compatible blob storage)
* Infrastructure: Docker, Docker Compose

---

## Key Engineering Decisions

### 1. Decoupled Storage (MinIO + PostgreSQL)
Storing massive text payloads (up to 10MB) inside a relational database causes severe row-chaining and degrades query performance. 
* The Solution: PostgreSQL strictly holds lightweight metadata (Author, Views, Expiration). The actual text payloads are pushed directly to a MinIO (S3) instance. 

### 2. Bounded-Memory Analytics
Tracking unique views for viral pastes can quickly exhaust system memory and expose sensitive user IP data.
* The Solution: View tracking is handled asynchronously using Redis HyperLogLog combined with SHA-256 hashing (IP + User-Agent). This ensures accurate, anonymized unique view counts while strictly capping memory usage at 12KB per paste, regardless of traffic volume.

### 3. Eventual Consistency for Database Protection
Writing every single page view directly to PostgreSQL would bottleneck the database during a traffic spike.
* The Solution: The backend utilizes an eventual consistency model. Views are written instantly to Redis. A Spring @Scheduled worker flushes these aggregated views to PostgreSQL every 5 minutes in a single batch, reducing thousands of database hits down to a single update.

### 4. Lazy Caching with Native LFU Eviction
To prevent cache thrashing and Out-Of-Memory (OOM) crashes, the application delegates eviction logic to the infrastructure layer.
* The Solution: Redis is configured with an `allkeys-lfu` (Least Frequently Used) memory limit. The Spring Boot backend safely hydrates the cache with fully assembled DTOs (DB metadata + MinIO payloads). When the cache fills up, Redis natively and atomically evicts the coldest pastes to make room for viral ones.

### 5. Thread-Safe Immutability
To prevent concurrency bugs when patching stale cache data (e.g., updating view counts on cached objects), all data transfer objects (DTOs) are implemented as Java records. 
* The Solution: State mutations are handled via the functional "Wither" pattern, ensuring deep immutability and thread-safe reads across thousands of concurrent requests. Timezone-safe expirations are strictly enforced using UTC Instant.

---

## Getting Started (Local Development)

### Prerequisites
* Java 21 or higher
* Docker and Docker Compose
* Gradle (or use the included wrapper)

### 1. Clone the repository
```bash
git clone [https://github.com/YourUsername/your-repo-name.git](https://github.com/YourUsername/your-repo-name.git)
cd your-repo-name
```
### 2. Configure Environment Variables
Copy the sample environment file to configure your local database and MinIO credentials:
```bash
cp .env.example .env
```
### 3. Start the Infrastructure
Spin up PostgreSQL, Redis, and MinIO locally:
```bash
docker-compose up -d
```
### 4. Run the Application
Start the Spring Boot server using the Gradle wrapper:
```bash
./gradlew bootRun
```
