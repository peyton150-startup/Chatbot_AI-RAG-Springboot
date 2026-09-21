# Chatbot AI RAG (Spring Boot)

A Spring Boot chatbot for a service business. It combines retrieval-augmented generation (RAG) with OpenAI, a PostgreSQL-backed admin area, lead capture, configurable branding, conversation history, ratings, and analytics.

> [!IMPORTANT]
> The repository compiles, but the current `main` branch does **not** start successfully without code changes. See [Validation status](#validation-status) and [Known blockers](#known-blockers) before deploying it.

## Features

- Public chatbot widget with a responsive chat window
- RAG over a bundled `vectors.json` knowledge base
- OpenAI embeddings (`text-embedding-3-large`) and chat completions (`gpt-4o-mini`)
- Three-turn conversation memory keyed by HTTP session
- Lead capture with name, email, timestamp, and session ID
- Per-user colors, avatar, banner, suggestions, and booking URL
- Admin-managed users and selection of the public-facing theme
- Chat logs, ratings, popular-question statistics, daily counts, and CSV exports
- Hot reload of an uploaded vector store from the admin dashboard
- English, Spanish, French, German, Portuguese, Chinese, and Arabic widget copy
- In-process rate limiting for the public chat API

## Architecture

```text
Browser / embedded widget
        |
        | HTTP
        v
Spring MVC + Spring Security
        |
        +--> PostgreSQL (users, themes, leads, chat logs, app settings)
        |
        +--> VectorStore (89 bundled 3,072-dimensional embeddings)
        |
        +--> OpenAI embeddings + chat completions
```

| Area | Main implementation |
| --- | --- |
| Application startup | `ChatbotApplication.java` |
| Public chat API | `chat/ChatController.java` |
| Retrieval and prompting | `rag/RAGService.java`, `rag/VectorStore.java` |
| Authentication and authorization | `config/SecurityConfig.java` |
| Admin dashboard | `admin/AdminController.java`, `templates/admin.html` |
| Themes | `theme/*`, `config/AppSettings*` |
| Leads | `leads/*` |
| Analytics | `analytics/*`, `templates/analytics.html` |
| Public widget | `templates/index.html`, `static/css/chatbotEmbed.js` |

## Technology

- Java 17
- Spring Boot 3.2.3
- Spring MVC, Spring Security, Spring Data JPA, and Thymeleaf
- PostgreSQL
- Maven Wrapper 3.9.12
- OpenAI Java client (`com.theokanning:openai-gpt3-java:0.11.0`)
- Bucket4j
- Docker multi-stage build

## Prerequisites

- Java 17, or Docker Desktop
- PostgreSQL
- An OpenAI API key with access to the embedding and chat models
- Maven is optional because the Maven Wrapper is included

## Configuration

The application reads these environment variables:

| Variable | Required | Description |
| --- | --- | --- |
| `OPENAI_API_KEY` | Yes for chat | Used for embeddings and chat completions |
| `DB_URL` | Yes | JDBC URL, for example `jdbc:postgresql://localhost:5432/chatbot` |
| `DB_USER` | Yes | PostgreSQL username |
| `DB_PASSWORD` | Yes | PostgreSQL password |
| `PORT` | No | HTTP port; defaults to `8080` |
| `BOOTSTRAP_ADMIN_USERNAME` | No | Initial administrator username |
| `BOOTSTRAP_ADMIN_EMAIL` | No | Initial administrator email |
| `BOOTSTRAP_ADMIN_PASSWORD` | No | Initial administrator password |

Set all three `BOOTSTRAP_ADMIN_*` variables together for the first startup, then remove them after the account has been created.

> [!NOTE]
> `src/main/resources/application-example.properties` currently shows `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`. Those names do not match the application configuration. Use `DB_URL`, `DB_USER`, and `DB_PASSWORD` unless the source configuration is changed.

Optional Spring overrides include:

- `APP_CHAT_MAX_QUESTION_LENGTH` (default `2000`)
- `APP_CHAT_RATE_LIMIT_PER_MINUTE` (default `20`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO`

## Database setup

The committed configuration uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

That is appropriate only when the schema already exists. This repository does not contain Flyway/Liquibase migrations or a schema SQL file, so a new empty database cannot be initialized by the checked-in project configuration.

For a disposable local database, `SPRING_JPA_HIBERNATE_DDL_AUTO=update` can let Hibernate create/update the tables. Do not rely on `update` as a production migration strategy; add versioned migrations first.

The JPA model expects these tables:

- `users`
- `chatbot_theme`
- `chat_log`
- `leads`
- `app_settings`

## Run locally with Java 17

In PowerShell:

```powershell
$env:OPENAI_API_KEY = "your-key"
$env:DB_URL = "jdbc:postgresql://localhost:5432/chatbot"
$env:DB_USER = "chatbot"
$env:DB_PASSWORD = "replace-me"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = "update" # local bootstrap only
$env:BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"
$env:BOOTSTRAP_ADMIN_PASSWORD = "use-a-long-unique-password"

.\mvnw.cmd spring-boot:run
```

On macOS or Linux, use `./mvnw spring-boot:run` and export the same variables.

When the startup blocker is fixed, the application is intended to be available at:

- Chatbot: `http://localhost:8080/`
- Login: `http://localhost:8080/login`
- Admin dashboard: `http://localhost:8080/admin`
- Analytics: `http://localhost:8080/admin/analytics`

## Build and run with Docker

Build the image:

```bash
docker build -t chatbot-ai-rag .
```

Run it against an existing PostgreSQL instance:

```bash
docker run --rm -p 8080:8080 \
  -e OPENAI_API_KEY=your-key \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/chatbot \
  -e DB_USER=chatbot \
  -e DB_PASSWORD=replace-me \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
  -e BOOTSTRAP_ADMIN_USERNAME=admin \
  -e BOOTSTRAP_ADMIN_EMAIL=admin@example.com \
  -e BOOTSTRAP_ADMIN_PASSWORD=use-a-long-unique-password \
  chatbot-ai-rag
```

The `SPRING_JPA_HIBERNATE_DDL_AUTO=update` setting above is for local evaluation only.

## API overview

| Method | Route | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/` | Public | Render the built-in chatbot |
| `POST` | `/api/chat` | Public | Ask a question using JSON or plain text |
| `POST` | `/api/leads` | Public | Capture a lead |
| `GET` | `/api/theme` | Public | Load the current public theme |
| `POST` | `/api/analytics/rate` | Public | Rate a logged answer with `1` or `-1` |
| `GET` | `/admin` | Admin | Admin dashboard |
| `GET` | `/admin/analytics` | Admin | Analytics dashboard |
| `GET` | `/api/analytics/*` | Admin | Logs, summary, sessions, and threads |
| `GET` | `/api/leads` | Admin | List captured leads |
| `POST` | `/admin/theme` | Admin | Save theme settings and images |
| `POST` | `/admin/active-theme` | Admin | Choose the public-facing user's theme |
| `POST` | `/admin/rag/upload` | Admin | Hot-reload an uploaded vector store |

Example chat request:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"What services do you offer?"}'
```

Expected response shape:

```json
{
  "id": 123,
  "answer": "..."
}
```

## Knowledge base

`src/main/resources/vectors.json` is loaded at startup. The checked-in file contains 89 entries, each with a 3,072-value embedding compatible with `text-embedding-3-large`.

Each entry has this shape:

```json
{
  "id": "page-or-chunk-id",
  "source": "Source name",
  "text": "Text used as retrieved context",
  "embedding": [0.0123, -0.0456]
}
```

The admin upload endpoint accepts a non-empty JSON array with the same `text` and `embedding` fields. Uploaded vectors are held in memory and are not written back to the repository or database, so they are lost on process restart.

The separate `pages.json` file contains 49 raw page records using `slug`, `title`, and `content`. The running RAG service does not load this file.

## Embedding the widget

The intended integration is:

```html
<script>
  window.HarmonyChatConfig = {
    serverUrl: "https://your-chatbot.example.com"
  };
</script>
<script src="https://your-chatbot.example.com/chatbot-embed.js" defer></script>
```

This integration is currently blocked by the resource-path mismatch and missing cross-origin API configuration described below.

## Validation status

Validation performed against the checked-out `main` branch on September 21, 2026:

| Check | Result |
| --- | --- |
| Repository inventory and source review | 53 project files inspected |
| Vector JSON parsing | 89/89 entries parsed; all have text/source and 3,072-dimensional embeddings |
| Raw page JSON parsing | 49 entries parsed |
| Docker image build | Pass |
| Java compilation | Pass: 37 source files compiled with Java 17 |
| Automated tests | No test sources exist; Maven reports `No tests to run` |
| Spring Boot startup with PostgreSQL | Fail |
| Browser/API end-to-end flow | Blocked because application startup fails |
| Live OpenAI response | Not attempted without a real API key and a running application |

Commands used for validation:

```bash
docker build --target build -t chatbot-ai-rag-check .
docker run --rm chatbot-ai-rag-check mvn test -B
docker build -t chatbot-ai-rag-runtime .
```

For the runtime check, the application image was launched with PostgreSQL 16 and `SPRING_JPA_HIBERNATE_DDL_AUTO=update` to isolate application startup from the missing migration files.

## Known blockers

1. **Application startup fails in `ChatRateLimiter`.** Spring reports `No default constructor found`. The component has two constructors, but neither constructor is explicitly selected for dependency injection. The application exits before accepting HTTP requests.
2. **The embeddable script route points to a missing classpath resource.** `EmbedController` requests `static/chatbot-embed.js`, while the repository contains `static/css/chatbotEmbed.js`.
3. **Cross-site embedding is not configured.** The widget calls the chatbot server from the host page, but no CORS configuration permits those cross-origin API requests.
4. **A fresh database has no supported initialization path.** `ddl-auto=validate` requires an existing schema, but no migrations or schema script are committed.
5. **The example database variable names are incorrect.** The example uses `DATABASE_*`; the application reads `DB_*`.
6. **Editing a user without entering a new password can re-hash the existing BCrypt hash.** `UserService.saveUser` encodes any non-blank password, including a hash already loaded from the database.
7. **There is no automated test suite.** Maven compiles the project successfully but runs zero tests.

Additional security review is recommended before production use. In particular, public theme responses currently serialize the associated user object, the public rating endpoint accepts any existing log ID, uploaded vector files are only spot-checked, and the in-memory per-IP rate-limit maps do not evict old entries.

## Suggested repair order

1. Select the intended Spring constructor in `ChatRateLimiter` and add a context-startup test.
2. Add database migrations and keep production on `ddl-auto=validate`.
3. Align the embedded script filename/path and configure an explicit CORS allowlist.
4. Correct `application-example.properties`.
5. Fix password update semantics and add service/controller tests.
6. Add integration tests covering login, admin authorization, lead capture, theme loading, chat logging, ratings, and RAG failure handling.

## Project layout

```text
.
├── Dockerfile
├── pom.xml
├── mvnw / mvnw.cmd
└── src/main
    ├── java/com/harmony/chatbot
    │   ├── admin
    │   ├── analytics
    │   ├── chat
    │   ├── config
    │   ├── leads
    │   ├── rag
    │   ├── theme
    │   └── user
    └── resources
        ├── application.properties
        ├── application-example.properties
        ├── pages.json
        ├── vectors.json
        ├── static
        └── templates
```

## License

No license file is currently included. Add a license before distributing the project if reuse terms need to be explicit.
