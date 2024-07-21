package io.ib67.ask.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class OpenAiChatModel {
    private final String apiKey;
    private final String model;
    private final String endpointUrl;
    private final boolean dedicatedContext;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final List<ChatMessage> session = new ArrayList<>();

    public OpenAiChatModel(String apiKey, String model, String endpointUrl, boolean dedicatedContext) {
        this.apiKey = apiKey;
        this.model = model;
        this.endpointUrl = endpointUrl;
        this.dedicatedContext = dedicatedContext;
        this.httpClient = HttpClient.newBuilder().build();
    }

    public void addMessage(ChatMessage message) {
        session.add(message);
    }

    public ChatMessage generate(ChatMessage... newMessage) {
        if (dedicatedContext) {
            var newContext = new ArrayList<>(session);
            newContext.addAll(List.of(newMessage));
            return generate(newContext);
        } else {
            session.addAll(List.of(newMessage));
            var result = generate(session);
            session.add(result);
            return result;
        }
    }

    private ChatMessage generate(Collection<? extends ChatMessage> session) {
        var jo = new JsonObject();
        jo.addProperty("model", model);
        jo.add("messages", gson.toJsonTree(session));
        var req = buildReq(URI.create(endpointUrl).resolve("chat/completions"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(jo))).build();
        var retries = 0;
        do {
            try {
                var resp = httpClient.send(req, ofString());
                if (resp.statusCode() != 200) {
                    System.out.println(resp.body());
                    throw new RuntimeException("Invalid status code: " + resp.statusCode());
                }
                var rsp = JsonParser.parseString(resp.body());
                var msg = rsp.getAsJsonObject().getAsJsonArray("choices")
                        .get(0).getAsJsonObject().getAsJsonObject("message");
                return gson.fromJson(msg, ChatMessage.class);
            } catch (IOException | InterruptedException e) {
                if (++retries > 3) {
                    throw new RuntimeException("Failed to POST", e);
                }
            }
        } while (true);

    }

    private HttpRequest.Builder buildReq(URI uri) {
        return HttpRequest.newBuilder(uri)
                .header("User-Agent", "ask/0.2.0")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey);

    }
}
