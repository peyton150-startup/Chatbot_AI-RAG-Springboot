# NVIDIA-Only RAG Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace OpenAI with NVIDIA Nemotron for embeddings and grounded answer generation, regenerate the bundled vectors, and verify a real question succeeds end to end.

**Architecture:** A provider-neutral `AiClient` interface separates RAG orchestration from `NvidiaAiClient`, which uses Java `HttpClient` and Jackson against NVIDIA's OpenAI-compatible endpoints. Typed configuration supplies secrets and model defaults, while a tested rebuild command converts every stored passage to the required 2,048-dimensional embedding space.

**Tech Stack:** Java 17, Spring Boot 3.2.3, Jackson, Java HttpClient, JUnit 5, Docker, PostgreSQL, NVIDIA NIM APIs

**Spec:** `docs/superpowers/specs/2026-09-21-nvidia-rag-design.md`

## Global Constraints

- Use `https://integrate.api.nvidia.com/v1` by default.
- Use `nvidia/nemotron-3-embed-1b` with `passage` for indexing and `query` for questions.
- Use `nvidia/nemotron-3.5-lightning-30b-a3b` for answer generation.
- Require exactly 2,048 embedding values by default.
- Read the bearer token from `NVIDIA_API_KEY`; never print, persist, or commit it.
- Write a failing behavioral test and observe the expected failure before each production behavior is added.

---

### Task 1: NVIDIA HTTP client and typed configuration

**Files:**
- Create: `src/main/java/com/harmony/chatbot/ai/AiClient.java`
- Create: `src/main/java/com/harmony/chatbot/ai/AiMessage.java`
- Create: `src/main/java/com/harmony/chatbot/ai/NvidiaAiProperties.java`
- Create: `src/main/java/com/harmony/chatbot/ai/NvidiaAiClient.java`
- Create: `src/test/java/com/harmony/chatbot/ai/NvidiaAiClientTest.java`
- Modify: `src/main/java/com/harmony/chatbot/ChatbotApplication.java`
- Modify: `pom.xml`

**Interfaces:**
- Produces: `double[] AiClient.embedQuery(String text)`
- Produces: `List<double[]> AiClient.embedPassages(List<String> texts)`
- Produces: `String AiClient.chat(List<AiMessage> messages)`
- Produces: typed `NvidiaAiProperties` defaults plus required API-key validation

- [ ] **Step 1: Write the failing HTTP contract tests**

Use an in-process `HttpServer` to return complete OpenAI-compatible fixtures and assert observable results plus captured request method, path, bearer header, model, `input_type`, and messages. Include separate tests for query embeddings, passage batches, chat completions, non-2xx responses, malformed responses, and a wrong embedding dimension.

```java
assertArrayEquals(new double[] { 0.25, 0.75 }, client.embedQuery("vaccinations"));
assertEquals("query", capturedBody.path("input_type").asText());
assertEquals("/v1/embeddings", capturedPath.get());
assertEquals("Bearer test-key", capturedAuthorization.get());
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `mvn -Dtest=NvidiaAiClientTest test`

Expected: test compilation fails because `AiClient`, `AiMessage`, `NvidiaAiProperties`, and `NvidiaAiClient` do not exist.

- [ ] **Step 3: Implement the minimal client and configuration**

Serialize request bodies with `ObjectMapper`, call `/embeddings` and `/chat/completions`, require HTTP 2xx, parse the standard `data[].embedding` and `choices[0].message.content` fields, validate dimensions, and throw a provider exception containing status/model context but never the key.

- [ ] **Step 4: Remove the obsolete OpenAI client dependency and enable properties scanning**

Delete `com.theokanning.openai-gpt3-java:client` from `pom.xml` and add `@ConfigurationPropertiesScan` to `ChatbotApplication`.

- [ ] **Step 5: Run focused and full tests and verify GREEN**

Run: `mvn -Dtest=NvidiaAiClientTest test`, then `mvn test`.

Expected: all NVIDIA client tests pass with no secret values in output.

### Task 2: Strict vector-store compatibility and RAG orchestration

**Files:**
- Modify: `src/main/java/com/harmony/chatbot/rag/VectorStore.java`
- Modify: `src/main/java/com/harmony/chatbot/rag/RAGService.java`
- Delete: `src/main/java/com/harmony/chatbot/chat/OpenAIService.java`
- Create: `src/test/java/com/harmony/chatbot/rag/VectorStoreTest.java`
- Create: `src/test/java/com/harmony/chatbot/rag/RAGServiceTest.java`

**Interfaces:**
- Consumes: `AiClient.embedQuery` and `AiClient.chat`
- Produces: `VectorStore(Page[] pages, int expectedDimensions)` with strict document/query validation
- Produces: constructor-injected `RAGService(AiClient, ChatLogRepository, ObjectMapper, NvidiaAiProperties)`

- [ ] **Step 1: Write failing dimension and retrieval tests**

Create literal two-dimensional fixtures to prove the nearest page is returned, then assert construction rejects a page of the wrong size and querying rejects a mismatched question vector.

```java
assertEquals("boarding", store.getTopNPages(new double[] { 1.0, 0.0 }, 1).get(0).getId());
assertThrows(IllegalArgumentException.class,
        () -> new VectorStore(new Page[] { pageWith(new double[] { 1.0 }) }, 2));
```

- [ ] **Step 2: Run `VectorStoreTest` and verify RED**

Expected: compilation fails because the dimension-aware constructor does not exist, or the mismatch assertion fails because the old code silently uses the shorter vector.

- [ ] **Step 3: Implement strict dimensions and verify GREEN**

Store `expectedDimensions`, validate every non-empty passage embedding during construction, validate each query before scoring, and compute dot products across the full known dimension.

- [ ] **Step 4: Write the failing RAG orchestration tests**

Use a small fake `AiClient` and mocked repository to prove the clean question is embedded, retrieved page text appears in the system message, the current question appears in the user message, and the client's chat result is returned. Add a failure-path test that produces the existing safe error response without leaking provider details.

- [ ] **Step 5: Run `RAGServiceTest` and verify RED**

Expected: compilation fails because `RAGService` still directly constructs the OpenAI client and cannot accept `AiClient`.

- [ ] **Step 6: Convert RAGService and verify GREEN**

Replace all `com.theokanning` types with `AiMessage`, call `embedQuery`, build the same grounding prompt/history, call `chat`, and use constructor injection for every dependency. Remove the unused `OpenAIService` class.

- [ ] **Step 7: Run all Task 2 tests**

Run: `mvn -Dtest=VectorStoreTest,RAGServiceTest test`, then `mvn test`.

Expected: all tests pass.

### Task 3: Tested vector regeneration command

**Files:**
- Create: `src/main/java/com/harmony/chatbot/rag/VectorFileRebuilder.java`
- Create: `src/main/java/com/harmony/chatbot/rag/VectorRebuildCommand.java`
- Create: `src/test/java/com/harmony/chatbot/rag/VectorFileRebuilderTest.java`
- Modify: `pom.xml`
- Modify: `src/main/resources/vectors.json`

**Interfaces:**
- Consumes: `AiClient.embedPassages(List<String>)`
- Produces: `void VectorFileRebuilder.rebuild(Path input, Path output)`
- Produces: CLI `VectorRebuildCommand <input-json> <output-json>` using `NVIDIA_API_KEY`

- [ ] **Step 1: Write the failing rebuild test**

Write two pages to a temporary JSON file, return literal embeddings from a fake client, run the rebuilder, then read the output and assert IDs/text/source are unchanged and both embeddings are replaced in original order.

```java
rebuilder.rebuild(input, output);
Page[] rebuilt = mapper.readValue(output.toFile(), Page[].class);
assertArrayEquals(new double[] { 0.1, 0.2 }, rebuilt[0].getEmbedding());
assertEquals("original source", rebuilt[0].getSource());
```

- [ ] **Step 2: Run the test and verify RED**

Expected: test compilation fails because `VectorFileRebuilder` does not exist.

- [ ] **Step 3: Implement atomic rebuild behavior and CLI**

Reject empty text, request passage embeddings in bounded batches, validate one result per page, write to a sibling temporary file, then atomically replace the requested output. The CLI reads only `NVIDIA_API_KEY` and never logs it.

- [ ] **Step 4: Add the Maven exec plugin and verify GREEN**

Configure `org.codehaus.mojo:exec-maven-plugin` so the command can run in the Docker build stage, then run `mvn -Dtest=VectorFileRebuilderTest test` and `mvn test`.

- [ ] **Step 5: Regenerate the 89 checked-in vectors with the live NVIDIA endpoint**

Load the key directly from the user-provided secret file into the Docker process environment and run the CLI against `src/main/resources/vectors.json`. Do not echo the value.

- [ ] **Step 6: Validate the regenerated artifact**

Parse the output and assert exactly 89 entries, every text/source is nonblank, every embedding contains exactly 2,048 finite values, and no secret-like `nvapi-` text occurs in the file.

### Task 4: Spring startup regression and configuration

**Files:**
- Modify: `src/main/java/com/harmony/chatbot/chat/ChatRateLimiter.java`
- Create: `src/test/java/com/harmony/chatbot/chat/ChatRateLimiterSpringTest.java`
- Modify: `src/main/resources/application.properties`
- Modify: `src/main/resources/application-example.properties`

**Interfaces:**
- Consumes: `app.ai.nvidia.*` typed properties
- Produces: a Spring-instantiable `ChatRateLimiter`

- [ ] **Step 1: Write the failing Spring construction test**

Start an `ApplicationContextRunner` with `ChatRateLimiter` and `app.chat.rate-limit-per-minute=7`, then assert the bean exists and permits exactly seven immediate acquisitions for one client.

- [ ] **Step 2: Run the test and verify RED**

Expected: context startup fails with `No default constructor found` because Spring cannot select between the two constructors.

- [ ] **Step 3: Select the intended constructor and verify GREEN**

Annotate the public `@Value` constructor with `@Autowired`, rerun the focused test, then run the entire suite.

- [ ] **Step 4: Replace OpenAI properties with NVIDIA properties**

Set `app.ai.nvidia.api-key=${NVIDIA_API_KEY}`, NVIDIA defaults for base URL/chat/embed models, `embedding-dimensions=2048`, and a bounded timeout. Correct the example database variables from `DATABASE_*` to the `DB_*` names consumed by the application.

### Task 5: Documentation and Docker end-to-end verification

**Files:**
- Modify: `README.md`
- Modify: `Dockerfile`

**Interfaces:**
- Documents: secure `NVIDIA_API_KEY` setup, vector rebuild command, Docker/PostgreSQL startup, and `/api/chat` smoke test
- Produces: Docker build that runs tests instead of permanently skipping them

- [ ] **Step 1: Update README and Docker build behavior**

Replace OpenAI architecture/configuration/model references, document why old vectors cannot be reused, show the rebuild command without embedding a key, and update the validation/blocker sections with current verified results. Change the build stage to run `mvn clean verify -B`.

- [ ] **Step 2: Run repository safety scans**

Run `rg -n "OPENAI_API_KEY|api.openai.com|text-embedding-3-large|gpt-4o-mini|nvapi-" .` and review every match. Expected: no OpenAI runtime references and no NVIDIA secret; `nvapi-` may appear only in a test assertion or safety documentation if necessary.

- [ ] **Step 3: Build and test with Docker**

Run `docker build --target build --tag chatbot-nvidia-build .`, `docker run --rm chatbot-nvidia-build mvn test -B`, and `docker build --tag chatbot-nvidia .`.

Expected: all automated tests and both image builds pass.

- [ ] **Step 4: Run disposable PostgreSQL and application containers**

Create an isolated Docker network, run PostgreSQL, then run the app with the key injected from the secret file, `DB_*` variables, and `SPRING_JPA_HIBERNATE_DDL_AUTO=update`. Do not print the key or inspect the environment.

- [ ] **Step 5: Exercise the real browser-facing endpoint**

POST a representative knowledge-base question to `/api/chat`, assert HTTP 200, a persisted numeric response ID, a non-error answer, and answer content consistent with a retrieved source passage. Inspect sanitized logs for loaded vector count and retrieved context count.

- [ ] **Step 6: Clean up disposable containers and report branch state**

Remove only the explicitly named test containers/network, run `git diff --check`, `git status --short`, and `git diff --stat`, and present the verified outcome. Do not push without separate user permission.
