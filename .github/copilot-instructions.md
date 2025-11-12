## Quick guide for AI coding agents (MicroserviceEcomerce)

This file contains concise, project-specific knowledge an AI needs to be productive in this repository.

- Project type: Java Spring Boot multi-module backend + React + Vite frontend.
- Top-level build: Maven (parent `pom.xml`). Java 21. Many modules are listed in the parent POM (see `pom.xml` modules element).

Key concepts and where to look
- Multi-module layout: root `pom.xml` lists modules: `auth-service`, `product-service`, `eureka-server`, `api-gateway`, `inventory-service`, `order-service`, `payment-service`, `cart-service`, `notification-service`.
- Frontend client: `microserviceclient/` (Vite + React + TypeScript). See `microserviceclient/README.md`.
- Container orchestration: `docker-compose.yml` at repository root configures DBs, Redis, Kafka, Prometheus, Grafana, and comments for Java services. Use the file as the single source for default ports and service wiring.
- Per-service hints and run notes: each service has a `HELP.md` (e.g. `api-gateway/HELP.md`, `product-service/HELP.md`) and usually a `Dockerfile` and `mvnw` wrapper.

Developer workflows (explicit, reproducible)
- Build all Java modules (skip tests for fast local work):
  - From repo root (Windows): `mvnw.cmd clean package -DskipTests` or `mvnw.cmd -T 1C clean package -DskipTests` for faster builds.
  - Or use `mvn clean package -DskipTests` if mvn is available.
- Run full stack with containers (DBs, Kafka, Redis, prometheus, grafana):
  - `docker compose up -d --build` from the repo root. See `docker-compose.yml` for service names and ports.
  - Useful compose commands (logs / stop): `docker compose logs -f` and `docker compose down -v`.
- Many Java services are commented out in the compose file with a note “CHẠY TRÊN INTELLIJ IDEA” — meaning developers usually run Java services in the IDE. Check each service's `HELP.md` to see recommended dev flow.

Ports & runtime facts (explicit examples from repo)
- API Gateway: expected on port 8080 (client targets `VITE_API_BASE_URL=http://localhost:8080`). See `microserviceclient/README.md`.
- Eureka: dashboard on 8761 (`docker-compose.yml`).
- Databases (mapped host ports): auth-db -> 3307, product-db -> 3308, order-db -> 3309, inventory-db -> 3310, cart-db -> 3311, notification-db -> 3312.
- Redis: 6379, Prometheus: 9097, Grafana: 3100.
- Actuator/debug ports are present for each service in compose comments (9090..9099 range) — use those when available for health/metrics.

Service integration patterns to respect
- Discovery: Eureka is used for service registration. Service-to-service calls expect Eureka to be present in dev flows.
- API Gateway: all client calls should go through the Gateway. Gateway enforces a secret header and handles auth routing. Example: client sends requests to `/api/...` via gateway (see `microserviceclient/README.md`).
- Auth: JWT-based auth. Gateway and services validate JWT; tokens returned by `auth-service`.
- Async messaging: Kafka + Zookeeper are defined in `docker-compose.yml` — some services depend on Kafka for events (see `order-service`, `inventory-service` HELP.md comments).

Coding & repository conventions
- Each service is a standard Spring Boot module with its own `pom.xml`, `Dockerfile`, and `HELP.md`. Prefer editing `HELP.md` and the module's `application-*.yml` to understand config keys.
- Use the included mvnw (`mvnw` / `mvnw.cmd`) in module folders to reproduce build commands reliably across developer machines.
- Environment variables: many runtime options come from `docker-compose.yml` env entries and `.env` files for the frontend. Search for `JWT_SECRET`, `GATEWAY_SECRET`, `SPRING_PROFILES_ACTIVE` when modifying auth/security behavior.

When making changes the AI should try first
1. Update or add unit tests inside the target module (`src/test` in the module) and run `mvnw.cmd -pl <module> test`.
2. Build the changed module: `mvnw.cmd -pl <module> -am clean package -DskipTests` (build module and its dependencies).
3. If the change affects runtime wiring (Eureka, Kafka, DB schema), prefer running the affected service locally in the IDE and bring only infra containers up via `docker compose up -d`.

Files you will commonly open
- `pom.xml` (root) — module list and JVM/tooling versions.
- `docker-compose.yml` — ports, container names, env variables, and which services are run in containers vs commented.
- `microserviceclient/README.md` — frontend dev flow and `VITE_API_BASE_URL` usage.
- `*/HELP.md` (each module) — per-service notes (build, run, docker hints).
- `api-gateway/` — gateway configuration; changes here affect all client->service routing and headers.

Notes / things NOT to assume
- Do not assume all services are started by docker-compose — several are commented and intended to run in IntelliJ (check `docker-compose.yml` comments and `HELP.md`).
- Secrets and AWS keys are injected via env vars — they are not present in the repo. Never hardcode secrets.

If anything below is unclear or you'd like me to expand a specific section (examples of gateway routes, common DTOs, or typical health endpoints), tell me which area to expand and I will iterate.
