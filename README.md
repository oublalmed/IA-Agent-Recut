# IA Recruiter Agent

MVP V1 — AI-powered recruitment automation platform.

## Tech Stack

- **Frontend**: Next.js 15, Tailwind CSS, Shadcn UI
- **Backend**: Spring Boot 3, Clean Architecture
- **Database**: PostgreSQL 16 + pgvector
- **Storage**: MinIO
- **Queue**: RabbitMQ
- **AI**: Anthropic Claude Haiku (claude-haiku-4-5-20251001)

## Quick Start (Local Dev)

### Prerequisites
- Docker & Docker Compose
- Java 21
- Node.js 22
- Maven 3.9+

### 1. Environment setup

```bash
cp .env.example .env
# Edit .env and set at minimum:
# - JWT_SECRET (base64, min 256 bits)
# - ANTHROPIC_API_KEY
# - NEXTAUTH_SECRET (min 32 chars)
```

### 2. Start infrastructure

```bash
cd infra
docker compose up -d postgres minio rabbitmq mailhog
```

Wait for services to be healthy:
```bash
docker compose ps
```

### 3. Run the API

```bash
cd apps/api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API available at: http://localhost:8080
Swagger UI: http://localhost:8080/api/swagger-ui.html

### 4. Run the Frontend

```bash
cd apps/web
cp .env.local.example .env.local
npm install
npm run dev
```

Frontend available at: http://localhost:3000

### 5. Dev tools

| Service | URL | Credentials |
|---------|-----|-------------|
| Swagger UI | http://localhost:8080/api/swagger-ui.html | — |
| MinIO Console | http://localhost:9001 | minioadmin / (see .env) |
| RabbitMQ Management | http://localhost:15672 | rabbit / (see .env) |
| MailHog | http://localhost:8025 | — |
| PostgreSQL | localhost:5432 | see .env |

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
│   └── web/          # Next.js 15 — App Router
├── infra/
│   ├── docker-compose.yml
│   ├── postgres/     # DB init scripts
│   └── rabbitmq/     # Queue definitions
└── .github/
    └── workflows/    # CI/CD
```

## Sprint Plan

| Sprint | Week | Scope |
|--------|------|-------|
| S0 | 1 | Foundations & Infra (this) |
| S1 | 2 | Auth & Multi-tenant |
| S2 | 3 | Companies & Team |
| S3 | 4 | Jobs |
| S4 | 5 | Resume Upload & Extraction |
| S5 | 6 | AI Matching & Scoring |
| S6 | 7 | Ranking & Dashboard |
| S7 | 8 | Compliance & Security |
| S8 | 9 | Quality & Release |

## API Reference

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/register | Register company + admin user |
| POST | /api/auth/login | Login (returns JWT) |
| POST | /api/auth/logout | Invalidate refresh token |
| GET | /api/users/me | Current user profile |
| GET | /api/companies/me | Company profile |
| PUT | /api/companies/me | Update company |
| GET | /api/jobs | List jobs |
| POST | /api/jobs | Create job |
| GET | /api/jobs/{id} | Get job detail |
| POST | /api/jobs/{id}/analyze | Trigger AI job analysis |
| POST | /api/resumes/upload | Upload CV (PDF/DOCX) |
| GET | /api/jobs/{jobId}/ranking | Get candidate ranking |
| POST | /api/ai/match | Trigger AI matching |
| POST | /api/applications/{id}/decision | Record recruiter decision |
| GET | /api/reports/dashboard | Dashboard KPIs |
| GET | /api/audit/decisions | Audit log |
| POST | /api/candidates/{id}/data-request | GDPR data request |

## Security Notes

- CV files are stored encrypted at rest (MinIO SSE)
- All inter-service traffic uses TLS in production
- Row-Level Security (RLS) enforces company isolation at DB level
- Audit decisions are immutable (INSERT only)
- No protected attributes (gender, age, origin) used in AI scoring
- Secrets via environment variables only — never committed
