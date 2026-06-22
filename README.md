# IA Recruiter Agent

MVP V1 — AI-powered recruitment automation platform.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Next.js 15, Tailwind CSS, Shadcn UI |
| Backend | Spring Boot 3, Clean Architecture |
| Database | PostgreSQL 16 + pgvector |
| Storage | MinIO (S3-compatible) |
| Queue | RabbitMQ |
| AI | Anthropic Claude (Haiku) |
| Auth | JWT + RBAC (ADMIN / RECRUITER / MANAGER) |

## Quick Start (Local Dev)

### Prerequisites

- Docker & Docker Compose
- Java 21
- Node.js 22
- Maven 3.9+

### 1. Environment setup

```bash
cp .env.example .env
# Required: set JWT_SECRET (base64, min 256 bits), ANTHROPIC_API_KEY, NEXTAUTH_SECRET
```

### 2. Start infrastructure

```bash
cd infra
docker compose up -d postgres minio rabbitmq mailhog
docker compose ps  # wait until all healthy
```

### 3. Run the API

```bash
cd apps/api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/api/swagger-ui.html

### 4. Run the Frontend

```bash
cd apps/web
cp .env.local.example .env.local
npm install
npm run dev
```

- Frontend: http://localhost:3000

### Dev Tools

| Service | URL | Credentials |
|---------|-----|-------------|
| Swagger UI | http://localhost:8080/api/swagger-ui.html | — |
| MinIO Console | http://localhost:9001 | see .env |
| RabbitMQ UI | http://localhost:15672 | see .env |
| MailHog | http://localhost:8025 | — |

### Full stack with Docker

```bash
cd infra
docker compose up -d
```

## Project Structure

```
ia-agent-recut/
├── apps/
│   ├── api/          # Spring Boot 3 — Clean Architecture
│   │   └── src/main/java/com/iarecruiter/
│   │       ├── auth/         # Auth module
│   │       ├── company/      # Company & team
│   │       ├── job/          # Jobs & analysis
│   │       ├── candidate/    # Resumes & candidates
│   │       ├── ai/           # AI engine (extraction, matching)
│   │       ├── reporting/    # Dashboard KPIs
│   │       ├── compliance/   # GDPR, audit log
│   │       └── shared/       # Security, config, exceptions
│   └── web/          # Next.js 15 — App Router
├── infra/
│   ├── docker-compose.yml
│   ├── postgres/     # DB init (extensions)
│   └── rabbitmq/     # Queue & exchange definitions
├── .github/
│   └── workflows/    # CI — backend & frontend
└── .env.example
```

## Sprint Plan

| Sprint | Week | Scope |
|--------|------|-------|
| **S0** | 1 | Foundations & Infra ✅ |
| S1 | 2 | Auth & Multi-tenant |
| S2 | 3 | Companies & Team |
| S3 | 4 | Jobs |
| S4 | 5 | Resume Upload & AI Extraction |
| S5 | 6 | AI Matching & Scoring |
| S6 | 7 | Ranking & Dashboard |
| S7 | 8 | Compliance & Security |
| S8 | 9 | Quality & Release |

## API Endpoints (V1)

```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/logout
POST   /api/auth/reset-password
GET    /api/users/me

GET    /api/companies/{id}
PATCH  /api/companies/{id}
POST   /api/companies/{id}/members

POST   /api/jobs
GET    /api/jobs
GET    /api/jobs/{id}
PATCH  /api/jobs/{id}
POST   /api/jobs/{id}/analyze

POST   /api/resumes/upload
GET    /api/resumes/{id}
DELETE /api/resumes/{id}       (GDPR erasure)
GET    /api/resumes/{id}/extracted-data

POST   /api/ai/match
GET    /api/jobs/{id}/ranking

GET    /api/reports/dashboard
GET    /api/audit/decisions
POST   /api/candidates/{id}/data-request
```

## Security

- CV files encrypted at rest (MinIO SSE) and in transit (TLS)
- Row-Level Security (RLS) enforces company data isolation at DB level
- Audit decisions are immutable (INSERT-only, no UPDATE/DELETE)
- No protected attributes (gender, age, origin) in AI scoring
- All secrets via environment variables — never committed
