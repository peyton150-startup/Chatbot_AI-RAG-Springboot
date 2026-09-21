# NVIDIA-Only RAG Design

## Goal

Replace the application's OpenAI dependency with NVIDIA-hosted, OpenAI-compatible APIs so a user can ask a question about the bundled knowledge base and receive an answer grounded in retrieved passages.

## Approved pipeline

1. Embed stored passages with `nvidia/nemotron-3-embed-1b` using `input_type=passage`.
2. Embed each question with the same model using `input_type=query`.
3. Retrieve the most similar passages with cosine similarity.
4. Send the question and retrieved passages to `nvidia/nemotron-3.5-lightning-30b-a3b`.
5. Return the standard OpenAI-compatible `choices[0].message.content` value.

Both APIs use `https://integrate.api.nvidia.com/v1` and a bearer token supplied only through `NVIDIA_API_KEY`.

## Compatibility requirements

- NVIDIA Nemotron Embed produces 2,048-dimensional vectors. The checked-in 3,072-dimensional OpenAI vectors must be regenerated.
- Indexing requests must use `input_type=passage`; question requests must use `input_type=query`.
- The application must reject mixed or mismatched vector dimensions instead of silently truncating the cosine-similarity calculation.
- The API key must never be written to source files, generated vector files, logs, documentation examples, image layers, or Git history.
- NVIDIA base URL, model identifiers, timeout, and expected embedding dimension remain configurable, with NVIDIA defaults.

## Implementation shape

- Introduce a small `AiClient` boundary used by the RAG service and vector rebuild tool.
- Implement the boundary with Java's HTTP client and Jackson, keeping the application independent of the obsolete OpenAI Java dependency.
- Keep provider configuration in typed Spring configuration properties.
- Provide a Java vector rebuild command that preserves page metadata and replaces embeddings atomically.
- Convert `RAGService` to constructor injection and use the new client.
- Select the intended Spring constructor in `ChatRateLimiter` so the application can start.
- Update the README and example configuration for the NVIDIA-only workflow.

## Verification

- Unit tests exercise the real JSON/HTTP boundary against an in-process HTTP server.
- Unit tests cover vector rebuilding, dimension validation, RAG orchestration, and Spring construction of the rate limiter.
- The 89 bundled vectors are regenerated with the user's locally stored NVIDIA key without printing or copying the secret.
- Docker runs the complete Maven test suite.
- PostgreSQL and the application run in disposable containers, followed by a real `/api/chat` request whose answer is checked for success and grounding.

