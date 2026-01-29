package com.harmony.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OpenAIService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL = "gpt-4.1-mini";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public String askGPT(String prompt) {
        try {
            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                return "OPENAI_API_KEY is not set";
            }

            String requestBody = """
            {
              "model": "%s",
              "input": "%s"
            }
            """.formatted(MODEL, prompt.replace("\"", "\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());

            // Defensive parsing (NO NPEs)
            JsonNode output = root.path("output");
            if (!output.isArray() || output.isEmpty()) {
                return "No output from OpenAI: " + response.body();
            }

            JsonNode content = output.get(0).path("content");
            if (!content.isArray() || content.isEmpty()) {
                return "No content from OpenAI: " + response.body();
            }

            return content.get(0).path("text").asText("No text returned");

        } catch (Exception e) {
            e.printStackTrace();
            return "Error contacting OpenAI API";
        }
    }
}
