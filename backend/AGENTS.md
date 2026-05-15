# Agent Configuration

## Overview
This project is a REST API server for a transport card (MIFARE) payment authorization system. 
The goal is to build a secure, containerized backend that handles transactions, card management, and terminal synchronization.

**Key Components:**
- REST API (Go / Gin or Echo)
- Database (SQLite3 with Goose migrations)
- Security (JWT Auth, TLS/HTTPS via Nginx)
- Documentation (Swagger)
- Deployment (Docker Incremental Build)

## Setup Instructions
- **Language:** Go 1.21+
- **Database:** SQLite3 (driver: `://github.com`)
- **Migrations:** Goose (`://github.com`)
- **HTTPS:** Port 8888 (localhost), requires self-signed certificates in `/nginx/certs`.
- **API Base URL:** `https://localhost:8888/api/v1`

## Architecture & Data Model
Follow a clean architecture or standard Go project layout (cmd/internal/pkg).

### Database Schema (Entities):
1. **Terminals:** ID, SerialNumber (unique), InstallationAddress, Name.
2. **Cards (MIFARE):** ID, CardNumber (unique), Balance, IsBlocked, OwnerName.
3. **Transactions:** ID, Amount, CardID (FK), TerminalID (FK), Timestamp.
4. **Keys:** ID, KeyValue, Description. Relationship: One Key to Many Cards.
5. **Users:** ID, Login, Password (hashed), IsAdmin (boolean).

## Coding Standards
- **Naming:** Use CamelCase for exported functions/structs, snake_case for JSON tags.
- **Error Handling:** Don't ignore errors. Use structured logging or clear error returns.
- **Security:** 
    - Use `bcrypt` for user passwords.
    - Implement JWT for all `/api/v1/admin/*` and protected routes.
    - Terminal auth functions must validate card balance before approving transactions.
- **REST:** 
    - Use standard HTTP status codes (200 OK, 201 Created, 403 Forbidden, 404 Not Found).
    - Implement CRUD for all tables.

## Common Tasks for Agent
1. **Bootstrap:** Create `go.mod`, folder structure, and initial `main.go`.
2. **Migrations:** Generate Goose migration files for all 5 tables.
3. **Auth:** Implement JWT middleware and `/login` endpoint.
4. **Terminal Logic:** 
    - `POST /auth-transaction`: Check card existence, balance >= amount, and block status.
    - `GET /fetch-keys`: Return all keys for terminal decryption.
5. **Docker:** Create a multi-stage Dockerfile that:
    - Builds the Go binary.
    - Installs Nginx.
    - Copies SSL certs and Nginx config.
    - Runs both Go app and Nginx (use a process manager like `supervisord` or a shell script).
6. **Swagger:** Use `swag init` to generate documentation at `/api/v1/swagger`.

## Deployment Requirements
- The final image must be an **incremental build**.
- Nginx must act as a reverse proxy for the Go application.
- TLS 1.2+ must be configured in Nginx.
