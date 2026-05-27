package edu.cit.gako.brainbox.modules.ai.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiCompatibleProviderTest {

    private final OpenAiCompatibleProvider provider = new OpenAiCompatibleProvider(new ObjectMapper());

    @Test
    void generateResponseBuildsChatCompletionsUrlOnceAndParsesContent() throws Exception {
        CapturingServer server = startServer(200, """
            {"choices":[{"message":{"content":"Hello from provider"}}]}
            """);

        try {
            String content = provider.generateResponse(
                server.baseUrl() + "/chat/completions/",
                "secret-key",
                "gpt-4o-mini",
                List.of(Map.of("role", "user", "content", "Hello")),
                0.4
            );

            assertEquals("Hello from provider", content);
            assertEquals("/v1/chat/completions", server.path.get());
            assertEquals("Bearer secret-key", server.authorization.get());
            assertEquals("application/json", server.contentType.get());
            assertTrue(server.requestBody.get().contains("\"model\":\"gpt-4o-mini\""));
        } finally {
            server.stop();
        }
    }

    @Test
    void generateResponseThrowsClearErrorForNonSuccessResponse() throws Exception {
        CapturingServer server = startServer(401, """
            {"error":{"message":"Invalid API key"}}
            """);

        try {
            RuntimeException error = assertThrows(RuntimeException.class, () -> provider.generateResponse(
                server.baseUrl(),
                "secret-key",
                "gpt-4o-mini",
                List.of(Map.of("role", "user", "content", "Hello")),
                0.4
            ));

            assertEquals("AI provider error (401): Invalid API key", error.getMessage());
        } finally {
            server.stop();
        }
    }

    @Test
    void generateResponseHandlesMissingChoicesSafely() throws Exception {
        CapturingServer server = startServer(200, "{}");

        try {
            RuntimeException error = assertThrows(RuntimeException.class, () -> provider.generateResponse(
                server.baseUrl(),
                "secret-key",
                "gpt-4o-mini",
                List.of(Map.of("role", "user", "content", "Hello")),
                0.4
            ));

            assertEquals("AI provider response did not include choices[0].message.content", error.getMessage());
        } finally {
            server.stop();
        }
    }

    private CapturingServer startServer(int statusCode, String responseBody) throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingServer server = new CapturingServer(httpServer);
        httpServer.createContext("/", (exchange) -> {
            server.path.set(exchange.getRequestURI().getPath());
            server.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            server.contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            server.requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        });
        httpServer.start();
        return server;
    }

    private static class CapturingServer {
        private final HttpServer httpServer;
        private final AtomicReference<String> path = new AtomicReference<>();
        private final AtomicReference<String> authorization = new AtomicReference<>();
        private final AtomicReference<String> contentType = new AtomicReference<>();
        private final AtomicReference<String> requestBody = new AtomicReference<>();

        private CapturingServer(HttpServer httpServer) {
            this.httpServer = httpServer;
        }

        private String baseUrl() {
            return "http://localhost:" + httpServer.getAddress().getPort() + "/v1";
        }

        private void stop() {
            httpServer.stop(0);
        }
    }
}
