package io.ib67.ask;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;

public class Ask {
    private final OpenAiChatModel model;
    private final ArrayList<ChatMessage> session = new ArrayList<>();

    public Ask(String apiKey, String endpointUrl, String model, String systemPrompt) {
        this.model = OpenAiChatModel.builder()
                .apiKey(requireNonNull(apiKey, "KEY cannot be null."))
                .baseUrl(endpointUrl)
                .modelName(model)
                .customHeaders(Map.of("User-Agent", "ask/0.1.0"))
                .build();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            session.add(new SystemMessage(systemPrompt));
        }
    }

    public void run(boolean interactive, String... initArgs) throws IOException {
        var prompt = String.join(" ", initArgs);
        var inPipe = System.in.available() > 0;
        if (!prompt.trim().isEmpty()) {
            if (inPipe) {
                session.add(new SystemMessage(prompt)); // in-pipe
            } else {
                askFromUser(prompt);
            }
        }
        if (interactive) {
            beginInteractive();
        } else if(inPipe) {
            askFromUser(new String(System.in.readAllBytes()));
        }
    }

    private void beginInteractive() {
        try (var scanner = new Scanner(System.in)) {
            System.out.print(">> ");
            while (scanner.hasNextLine()) {
                askFromUser(scanner.nextLine());
                System.out.print(">> ");
            }
        }
    }

    public void askFromUser(String prompt) {
        session.add(new UserMessage(prompt));
        var msg = model.generate(session).content();
        System.out.println(msg.text());
        session.add(msg);
    }
}
