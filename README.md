# Rate Limiting Microservice

A distributed Rate Limiting service built with **Spring Boot**, **Redis**, and **Docker**. This service implements the **Token Bucket Algorithm** to protect downstream APIs from abusive traffic.

## 🚀 Features
- **Algorithm:** Token Bucket (efficient and burst-tolerant).
- **Architecture:** Stateless Spring Boot service with Redis for state persistence.
- **Dockerized:** Fully containerized setup using Docker Compose.
- **CI/CD:** Automated testing and build pipeline using GitHub Actions.

## 🛠 Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3
- **Database:** Redis 6 (Alpine)
- **DevOps:** Docker, Docker Compose, GitHub Actions

## 🏃‍♂️ How to Run Locally
1. **Prerequisites:** Docker and Docker Compose installed.
2. **Clone the repo:**
   ```bash
   git clone [https://github.com/ganesh714/rate-limiter-microservice.git](https://github.com/ganesh714/rate-limiter-microservice.git)
   cd rate-limiter-microservice

```

3. **Start the services:**
```bash
docker-compose up --build

```


*The API will be available at `http://localhost:8080`.*

## 🧪 API Documentation

### Check Rate Limit

**Endpoint:** `POST /api/rate-limit`

**Request Body:**

```json
{
  "client_id": "user_123",
  "request_id": "req_abc"
}

```

**Response (Allowed):**

```json
{
  "status": "allowed",
  "client_id": "user_123",
  "remaining": 4,
  "reset_at": 1722076200
}

```

**Response (Denied):**

```json
{
  "status": "denied",
  "client_id": "user_123",
  "retry_after_seconds": 25
}

```

## ⚙️ Configuration

Configuration is managed via `application.properties` or Environment Variables in `docker-compose.yml`.

* `APP_RATE_LIMIT_CAPACITY`: Max requests (Default: 5)
* `APP_RATE_LIMIT_PERIOD_SECONDS`: Time window (Default: 60s)

