package com.unthinkable.resumescreener.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.unthinkable.resumescreener.config.OpenRouterProperties;
import com.unthinkable.resumescreener.exception.LlmException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real HTTP request/response handling against a stub HTTP server
 * (JDK-native com.sun.net.httpserver, no extra test dependency) instead of mocking
 * java.net.http.HttpClient directly.
 */
class OpenRouterLlmClientTest {

    private HttpServer server;
    private OpenRouterLlmClient client;
    private OpenRouterProperties properties;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();

        properties = new OpenRouterProperties();
        properties.setBaseUrl("http://localhost:" + server.getAddress().getPort());
        properties.setApiKey("test-key");
        properties.setTimeoutSeconds(5);

        client = new OpenRouterLlmClient(HttpClient.newHttpClient(), new ObjectMapper(), properties);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void returnsMessageContentOnSuccessfulResponse() {
        server.createContext("/chat/completions", exchange -> {
            String body = "{\"choices\":[{\"message\":{\"content\":\"{\\\"score\\\":8,\\\"justification\\\":\\\"good\\\"}\"}}]}";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        String content = client.complete("system prompt", "user prompt", "openai/gpt-4o-mini");

        assertThat(content).isEqualTo("{\"score\":8,\"justification\":\"good\"}");
    }

    @Test
    void throwsLlmExceptionOnNon200Response() {
        server.createContext("/chat/completions", exchange -> {
            byte[] bytes = "{\"error\":\"invalid model\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        assertThatThrownBy(() -> client.complete("system", "user", "bad/model"))
                .isInstanceOf(LlmException.class);
    }

    @Test
    void throwsLlmExceptionWhenResponseHasNoChoices() {
        server.createContext("/chat/completions", exchange -> {
            byte[] bytes = "{\"choices\":[]}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        assertThatThrownBy(() -> client.complete("system", "user", "openai/gpt-4o-mini"))
                .isInstanceOf(LlmException.class);
    }

    @Test
    void sendsModelAndMessagesInRequestBody() throws Exception {
        StringBuilder capturedBody = new StringBuilder();
        server.createContext("/chat/completions", exchange -> {
            capturedBody.append(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        client.complete("system prompt text", "user prompt text", "anthropic/claude-3.5-sonnet");

        assertThat(capturedBody.toString())
                .contains("\"model\":\"anthropic/claude-3.5-sonnet\"")
                .contains("system prompt text")
                .contains("user prompt text");
    }
}
