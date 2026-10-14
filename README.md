# Webhook Delivery Platform
![CI](https://github.com/Ericcodesinjava/webhook-platform/actions/workflows/ci.yml/badge.svg)

A multi-tenant webhook delivery service built with Spring Boot 4. It accepts events,
delivers them to customer endpoints with HMAC-signed payloads, retries failures with
exponential backoff, and dead-letters events that keep failing. It is the kind of
infrastructure companies like Stripe, GitHub and Shopify run behind their webhooks.

## Architecture

```
 Client (API key)
      |
      v
 POST /events
      |
      v
 [Rate limiter: Redis]  --over 100/min-->  HTTP 429 (Retry-After)
      |
      v
 [Save event: PostgreSQL]
      |
      v
 [Kafka topic: webhook.events]   key = tenantId, value = eventId
      |
      v
 [DeliveryConsumer]  finds endpoints subscribed to this event type
      |
      v
 [WebhookSender]  HMAC-SHA256 signature + per-endpoint circuit breaker
      |
      +-- success --> record SUCCESS
      |
      +-- failure --> record FAILED + next_retry_at
                          |
                          v
                 [RetryScheduler, every 10s]
                 backoff: 30s, 2min, 10min, 30min
                          |
                          v
                 after 5 attempts: DEAD_LETTERED
```

**How it fits together**
- **API layer:** accepts events and exposes delivery history. It never delivers directly.
- **Kafka:** separates ingestion from delivery, so a slow or dead destination cannot slow down the API, and queued events survive a crash.
- **Redis:** shared per-tenant counter for rate limiting.
- **PostgreSQL:** source of truth for events, endpoints, API keys and delivery attempts.
- **Resilience4j:** one circuit breaker per endpoint, so one broken destination never blocks healthy ones.

## Tech stack
Java 21, Spring Boot 4, Spring Data JPA, PostgreSQL, Apache Kafka (KRaft mode), Redis,
Spring Security (API keys), Resilience4j, springdoc-openapi (Swagger UI), Docker Compose,
GitHub Actions.

## Features
- Multi-tenant endpoint registration with per-endpoint event-type subscriptions
- Event ingestion API with per-tenant rate limiting (HTTP 429 with `Retry-After`)
- HMAC-SHA256 signed deliveries (`X-Webhook-Signature` header), secret per endpoint
- Retries with backoff (30s, 2min, 10min, 30min), 5 attempts, then dead-lettered
- Per-endpoint circuit breakers
- Full delivery attempt history per event
- API-key authentication, OpenAPI docs, Actuator health and metrics

## API overview
| Method | Path | Purpose |
|---|---|---|
| POST | `/events` | Submit an event for delivery |
| GET | `/events/{id}/deliveries` | Delivery attempt history for an event |
| POST | `/endpoints` | Register a webhook endpoint |
| GET | `/endpoints` | List endpoints |
| PATCH | `/endpoints/{id}` | Update an endpoint |
| DELETE | `/endpoints/{id}` | Remove an endpoint |

All endpoints require the `X-API-Key` header, except `/actuator/health` and the Swagger pages.

## Run it
1. Copy `.env.example` to `.env` and set `DB_PASSWORD` to any password you like
2. Start everything: `docker compose up --build`
3. Copy the `DEV API KEY` printed in the app log
4. Open http://localhost:8080/swagger-ui/index.html, click **Authorize**, paste the key

Run the tests (no Docker needed, they use H2 and mocks): `./mvnw -B verify`

## Try it in 2 minutes
1. Get a free test URL from https://webhook.site
2. In Swagger, click Authorize and paste the DEV API KEY from the app log
3. `POST /endpoints` with your webhook.site URL and `subscribedEventTypes` `["payment.succeeded"]`
4. `POST /events` with `eventType` `payment.succeeded`
5. Watch the signed request arrive on webhook.site
6. `GET /events/{id}/deliveries` to see the attempt history

## Key design decisions
- **Multi-tenancy:** shared tables with a `tenant_id` column on every row.
- **Retries:** a `next_retry_at` column plus a scheduler. Simple and Spring-native, with no delayed-queue infrastructure.
- **Kafka message key = tenantId:** keeps one tenant's events ordered within a partition.
- **Kafka message value = event ID only:** the payload stays in Postgres, so messages stay small.
- **Rate limiting:** Redis fixed window, 100 events per tenant per minute. Fails open if Redis is down, so a broken limiter cannot take the platform down.
- **Circuit breakers are per endpoint** (one shared config template), so a failing destination cannot block healthy ones.
- **Ordering:** best-effort per endpoint, not strict.

## Known limitations
- **Dual write:** an event is saved to Postgres and then published to Kafka. A crash in between can lose a delivery. The fix is the transactional outbox pattern.
- **At-least-once delivery:** receivers should de-duplicate on the event ID.
- **Fixed-window rate limiting** allows short bursts at window edges.
- **The consumer holds a DB connection open** during the webhook HTTP call.
- Retries run one after another on the scheduler thread.

## Scope decisions
- OAuth2 is not used. This is a service-to-service API, so API keys are the right fit. A user-login admin dashboard would be a separate module.
- Dev API key seeding replaces a real key-issuance flow (documented simplification).
- Single Kafka broker with replication factor 1. Production would use 3 brokers.