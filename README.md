# Chatbot AI RAG (Spring Boot)

A Spring Boot chatbot for a service business. It combines retrieval-augmented generation (RAG) with NVIDIA Nemotron, a PostgreSQL-backed admin area, lead capture, configurable branding, conversation history, ratings, and analytics.

> [!IMPORTANT]
> The NVIDIA API key is required at startup. The bundled vectors were generated with `nvidia/nemotron-3-embed-1b` and must not be mixed with embeddings from another model.

## Features

- Public chatbot widget with a responsive chat window
- RAG over a bundled `vectors.json` knowledge base
- NVIDIA query/passage embeddings (`nvidia/nemotron-3-embed-1b`)
- NVIDIA answer generation (`nvidia/nemotron-3.5-lightning-30b-a3b`)
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
        +--> VectorStore (89 bundled 2,048-dimensional embeddings)
        |
        +--> NVIDIA embeddings + Nemotron Lightning chat completions
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
- NVIDIA OpenAI-compatible APIs through Java `HttpClient`
- Bucket4j
- Docker multi-stage build

## Prerequisites

- Java 17, or Docker Desktop
- PostgreSQL
- An NVIDIA API key with access to the embedding and chat models
- Maven is optional because the Maven Wrapper is included

## Configuration

The application reads these environment variables:

| Variable | Required | Description |
| --- | --- | --- |
| `NVIDIA_API_KEY` | Yes | Bearer token used for embeddings and chat completions |
| `DB_URL` | Yes | JDBC URL, for example `jdbc:postgresql://localhost:5432/chatbot` |
| `DB_USER` | Yes | PostgreSQL username |
| `DB_PASSWORD` | Yes | PostgreSQL password |
| `PORT` | No | HTTP port; defaults to `8080` |
| `BOOTSTRAP_ADMIN_USERNAME` | No | Initial administrator username |
| `BOOTSTRAP_ADMIN_EMAIL` | No | Initial administrator email |
| `BOOTSTRAP_ADMIN_PASSWORD` | No | Initial administrator password |

Set all three `BOOTSTRAP_ADMIN_*` variables together for the first startup, then remove them after the account has been created.

Optional Spring overrides include:

- `APP_CHAT_MAX_QUESTION_LENGTH` (default `2000`)
- `APP_CHAT_RATE_LIMIT_PER_MINUTE` (default `20`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `NVIDIA_BASE_URL` (default `https://integrate.api.nvidia.com/v1`)
- `NVIDIA_CHAT_MODEL` (default `nvidia/nemotron-3.5-lightning-30b-a3b`)
- `NVIDIA_EMBEDDING_MODEL` (default `nvidia/nemotron-3-embed-1b`)
- `NVIDIA_EMBEDDING_DIMENSIONS` (default `2048`)
- `NVIDIA_API_TIMEOUT` (default `120s`)

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
$env:NVIDIA_API_KEY = (Get-Content -Raw "C:\secure\nvidia-key.txt").Trim()
$env:DB_URL = "jdbc:postgresql://localhost:5432/chatbot"
$env:DB_USER = "chatbot"
$env:DB_PASSWORD = "replace-me"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = "update" # local bootstrap only
$env:BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"
$env:BOOTSTRAP_ADMIN_PASSWORD = "use-a-long-unique-password"

.\mvnw.cmd spring-boot:run
```

The key file in that example must contain only the `nvapi-...` token. If your secret file also contains a label such as `NVIDIA_API_KEY=`, extract the token or copy only its value into the environment variable; passing the whole labeled line results in HTTP 401.

On macOS or Linux, use `./mvnw spring-boot:run` and export the same variables.

The application is then available at:

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
  -e NVIDIA_API_KEY \
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

Passing `-e NVIDIA_API_KEY` reads the value from the host environment without placing the key in the command or image.

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

`src/main/resources/vectors.json` is loaded at startup. The checked-in file contains 89 entries, each with a 2,048-value embedding generated by `nvidia/nemotron-3-embed-1b`.

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

The vector store validates every uploaded or bundled vector. A file created by a different model or with a dimension other than 2,048 is rejected instead of being compared incorrectly.

### Rebuild the vectors

Set `NVIDIA_API_KEY`, then run the tested rebuild command from the repository root:

```powershell
$env:NVIDIA_API_KEY = (Get-Content -Raw "C:\secure\nvidia-key.txt").Trim()
./mvnw.cmd -DskipTests compile exec:java `
  "-Dexec.mainClass=com.harmony.chatbot.rag.VectorRebuildCommand" `
  "-Dexec.args=src/main/resources/vectors.json src/main/resources/vectors.json"
Remove-Item Env:NVIDIA_API_KEY
```

The command embeds page text with `input_type=passage`, processes bounded batches, writes a temporary file, and replaces the output only after every batch succeeds. At question time the application uses `input_type=query`, which is required for accurate retrieval.

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

Validation performed against the `codex/nvidia-rag` branch on September 21, 2026:

| Check | Result |
| --- | --- |
| Repository inventory and source review | 53 project files inspected |
| Vector JSON parsing | 89/89 entries parsed; all have text/source and 2,048-dimensional NVIDIA embeddings |
| Raw page JSON parsing | 49 entries parsed |
| Docker image build | Pass |
| Java compilation | Pass with Java 17 |
| Automated tests | Pass: provider, vector store, RAG, rebuild, and Spring-construction tests |
| NVIDIA embedding authentication | Pass; live endpoint returned 2,048 dimensions |
| NVIDIA Lightning authentication | Pass; live endpoint returned chat content |
| Spring Boot startup with PostgreSQL | Pass in Docker against PostgreSQL 16 using the documented local schema override |
| RAG API end-to-end flow | Pass; live `/api/chat` request embedded the question, retrieved the four-week refund policy, generated a concise answer, and persisted the chat log |
| Reasoning-output safety | Pass; Nemotron thinking is disabled for customer-facing chat responses |

Commands used for validation:

```bash
docker build --target build -t chatbot-nvidia-build .
docker run --rm chatbot-nvidia-build mvn test -B
docker build -t chatbot-nvidia .
```

For the runtime check, the application image was launched with PostgreSQL 16 and `SPRING_JPA_HIBERNATE_DDL_AUTO=update` to isolate application startup from the missing migration files.

## Known blockers

1. **The embeddable script route points to a missing classpath resource.** `EmbedController` requests `static/chatbot-embed.js`, while the repository contains `static/css/chatbotEmbed.js`.
2. **Cross-site embedding is not configured.** The widget calls the chatbot server from the host page, but no CORS configuration permits those cross-origin API requests.
3. **A fresh database has no production migration path.** `ddl-auto=validate` requires an existing schema, but no migrations or schema script are committed. Use `update` only for local evaluation.
4. **Editing a user without entering a new password can re-hash the existing BCrypt hash.** `UserService.saveUser` encodes any non-blank password, including a hash already loaded from the database.

Additional security review is recommended before production use. In particular, public theme responses currently serialize the associated user object, the public rating endpoint accepts any existing log ID, uploaded vector files are only spot-checked, and the in-memory per-IP rate-limit maps do not evict old entries.

## Suggested repair order

1. Add database migrations and keep production on `ddl-auto=validate`.
2. Align the embedded script filename/path and configure an explicit CORS allowlist.
3. Fix password update semantics and add service/controller tests.
4. Add integration tests covering login, admin authorization, lead capture, theme loading, chat logging, ratings, and RAG failure handling.

## Project layout

```text
.
├── Dockerfile
├── pom.xml
├── mvnw / mvnw.cmd
└── src/main
    ├── java/com/harmony/chatbot
    │   ├── admin
    │   ├── ai
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
└── src/test/java/com/harmony/chatbot
    ├── ai
    ├── chat
    └── rag
```

## License

No license file is currently included. Add a license before distributing the project if reuse terms need to be explicit.
