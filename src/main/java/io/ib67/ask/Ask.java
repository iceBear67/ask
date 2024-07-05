package io.ib67.ask;

import io.ib67.ask.ai.ChatMessage;
import io.ib67.ask.ai.OpenAiChatModel;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;

public class Ask {
    private final OpenAiChatModel model;

    public Ask(Main.Config cfg, boolean dedicatedContext) {
        this.model = new OpenAiChatModel(
                requireNonNull(cfg.apiKey(), "KEY cannot be null."),
                cfg.model(),
                cfg.endpointUrl(),
                dedicatedContext
        );

        if (cfg.systemPrompt() != null && !cfg.systemPrompt().isBlank()) {
            model.addMessage(ChatMessage.ofSystemMessage(cfg.systemPrompt()));
        }
    }

    public void run(boolean interactive, String prompt) throws IOException {
        var inPipe = System.console() == null;
        if (!prompt.trim().isEmpty()) {
            if (inPipe) {
                model.addMessage(ChatMessage.ofSystemMessage(prompt)); // in-pipe
            } else {
                askFromUser(prompt);
            }
        }
        if (inPipe) {
            if (interactive) {
                beginInteractive(false);
            } else {
                try (var bi = new BufferedInputStream(new FileInputStream(FileDescriptor.in))) {
                    askFromUser(new String(bi.readAllBytes()));
                }
            }
        } else if (interactive) {
            beginInteractive(true);
        }
    }

    private void beginInteractive(boolean prompt) {
        try (var scanner = new Scanner(System.in)) {
            if (prompt) System.out.print(">> ");
            while (scanner.hasNextLine()) {
                askFromUser(scanner.nextLine());
                if (prompt) System.out.print(">> ");
            }
        }
    }

    public void askFromUser(String prompt) {
        var msg = model.generate(ChatMessage.ofUserMessage(prompt));
        System.out.println(msg.content());
    }
}
