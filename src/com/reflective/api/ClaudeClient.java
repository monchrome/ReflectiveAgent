package com.reflective.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.reflective.config.AppConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class ClaudeClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson       GSON        = new Gson();

    private ClaudeClient() {}

    /** Sends a single user message to Claude and streams the response to stdout while collecting it. */
    public static String send(String prompt) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.API_URL))
                .header("Content-Type",      "application/json")
                .header("x-api-key",         AppConfig.API_KEY)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(buildRequestBody(prompt))))
                .build();

        HttpResponse<InputStream> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            try (InputStream errStream = response.body()) {
                String err = new String(errStream.readAllBytes());
                throw new RuntimeException("API error " + response.statusCode() + ": " + err);
            }
        }

        return streamResponse(response);
    }

    private static JsonObject buildRequestBody(String prompt) {
        JsonObject body = new JsonObject();
        body.addProperty("model", AppConfig.MODEL);
        body.addProperty("max_tokens", AppConfig.MAX_TOKENS);
        body.addProperty("stream", true);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(userMsg);
        body.add("messages", messages);
        return body;
    }

    private static String streamResponse(HttpResponse<InputStream> response) throws Exception {
        StringBuilder full = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(response.body()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;
                String data = line.substring(6).trim();
                if (data.equals("[DONE]")) break;
                try {
                    JsonObject event = GSON.fromJson(data, JsonObject.class);
                    String type = event.get("type").getAsString();
                    if ("content_block_delta".equals(type)) {
                        JsonObject delta = event.getAsJsonObject("delta");
                        if ("text_delta".equals(delta.get("type").getAsString())) {
                            String text = delta.get("text").getAsString();
                            System.out.print(text);
                            System.out.flush();
                            full.append(text);
                        }
                    }
                } catch (Exception ignored) { /* skip malformed SSE lines */ }
            }
        }
        System.out.println();
        return full.toString();
    }
}