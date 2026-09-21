package com.harmony.chatbot.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NvidiaAiClientTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<JsonNode> requestBody = new AtomicReference<>();
    private HttpServer server;
    private int responseStatus;
    private String responseBody;

    @BeforeEach
    void startServer() throws IOException {
        responseStatus = 200;
        responseBody = "{}";
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/", this::handle);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void embedsAQuestionUsingQueryModeAndBearerAuthentication() {
        responseBody = """
                {"data":[{"index":0,"embedding":[0.25,0.75]}],"model":"nvidia/nemotron-3-embed-1b"}
                """;

        double[] result = client().embedQuery("vaccinations");

        assertArrayEquals(new double[]{0.25, 0.75}, result);
        assertEquals("/v1/embeddings", requestPath.get());
        assertEquals("Bearer test-key", authorization.get());
        assertEquals("nvidia/nemotron-3-embed-1b", requestBody.get().path("model").asText());
        assertEquals("vaccinations", requestBody.get().path("input").get(0).asText());
        assertEquals("query", requestBody.get().path("input_type").asText());
        assertEquals("float", requestBody.get().path("encoding_format").asText());
        assertEquals("END", requestBody.get().path("truncate").asText());
    }

    @Test
    void embedsPassagesInInputOrderUsingPassageMode() {
        responseBody = """
                {"data":[
                  {"index":1,"embedding":[0.30,0.40]},
                  {"index":0,"embedding":[0.10,0.20]}
                ]}
                """;

        List<double[]> result = client().embedPassages(List.of("first", "second"));

        assertArrayEquals(new double[]{0.10, 0.20}, result.get(0));
        assertArrayEquals(new double[]{0.30, 0.40}, result.get(1));
        assertEquals("passage", requestBody.get().path("input_type").asText());
        assertEquals("first", requestBody.get().path("input").get(0).asText());
        assertEquals("second", requestBody.get().path("input").get(1).asText());
    }

    @Test
    void returnsStandardOpenAiCompatibleChatContent() {
        responseBody = """
                {"choices":[{"index":0,"message":{"role":"assistant","content":"We offer daily dog walks."}}]}
                """;

        String result = client().chat(List.of(
                new AiMessage("system", "Use only the supplied context."),
                new AiMessage("user", "Do you offer walks?")
        ));

        assertEquals("We offer daily dog walks.", result);
        assertEquals("/v1/chat/completions", requestPath.get());
        assertEquals("nvidia/nemotron-3.5-lightning-30b-a3b", requestBody.get().path("model").asText());
        assertEquals("system", requestBody.get().path("messages").get(0).path("role").asText());
        assertEquals("Do you offer walks?", requestBody.get().path("messages").get(1).path("content").asText());
        assertFalse(requestBody.get().path("chat_template_kwargs").path("enable_thinking").asBoolean(true));
    }

    @Test
    void rejectsProviderErrorsWithoutLeakingTheApiKey() {
        responseStatus = 401;
        responseBody = "{\"detail\":\"unauthorized\"}";

        AiProviderException error = assertThrows(AiProviderException.class,
                () -> client().embedQuery("question"));

        assertTrue(error.getMessage().contains("401"));
        assertFalse(error.getMessage().contains("test-key"));
    }

    @Test
    void rejectsMalformedChatResponses() {
        responseBody = "{\"choices\":[]}";

        AiProviderException error = assertThrows(AiProviderException.class,
                () -> client().chat(List.of(new AiMessage("user", "question"))));

        assertTrue(error.getMessage().contains("chat response"));
    }

    @Test
    void rejectsEmbeddingVectorsWithTheWrongDimension() {
        responseBody = "{\"data\":[{\"index\":0,\"embedding\":[0.25]}]}";

        AiProviderException error = assertThrows(AiProviderException.class,
                () -> client().embedQuery("question"));

        assertTrue(error.getMessage().contains("expected 2"));
    }

    private NvidiaAiClient client() {
        NvidiaAiProperties properties = new NvidiaAiProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
        properties.setChatModel("nvidia/nemotron-3.5-lightning-30b-a3b");
        properties.setEmbeddingModel("nvidia/nemotron-3-embed-1b");
        properties.setEmbeddingDimensions(2);
        properties.setTimeout(Duration.ofSeconds(5));
        return new NvidiaAiClient(HttpClient.newHttpClient(), mapper, properties);
    }

    private void handle(HttpExchange exchange) throws IOException {
        requestPath.set(exchange.getRequestURI().getPath());
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        requestBody.set(mapper.readTree(exchange.getRequestBody()));
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(responseStatus, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
