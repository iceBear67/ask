package io.ib67.ask;

import io.ib67.ask.ai.ChatMessage;
import io.ib67.ask.ai.OpenAiChatModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;

public class Ask {
    private final OpenAiChatModel model;
    private final ArrayList<ChatMessage> session = new ArrayList<>();

    public Ask(String apiKey, String endpointUrl, String model, String systemPrompt) {
        this.model = new OpenAiChatModel(
                requireNonNull(apiKey, "KEY cannot be null."),
                model,
                endpointUrl
        );

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            session.add(ChatMessage.ofSystemMessage(systemPrompt));
        }
    }

    public void run(boolean interactive, String... initArgs) throws IOException {
        var prompt = String.join(" ", initArgs);
        var inPipe = System.in.available() > 0;
        if (!prompt.trim().isEmpty()) {
            if (inPipe) {
                session.add(ChatMessage.ofSystemMessage(prompt)); // in-pipe
            } else {
                askFromUser(prompt);
            }
        }
        if (interactive) {
            beginInteractive();
        } else if (inPipe) {
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
        session.add(ChatMessage.ofUserMessage(prompt));
        var msg = model.generate(session);
        System.out.println(msg.content());
        session.add(msg);
    }
}
