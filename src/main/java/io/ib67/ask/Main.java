package io.ib67.ask;

import com.google.gson.Gson;
import io.ib67.ask.ai.ChatMessage;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class Main {

    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws IOException, NoSuchMethodException {
        System.setProperty("slf4j.internal.verbosity", "ERROR"); // 吵得要死
        GSON.toJson(new ChatMessage("0", "1")); // for AOT
        Function.identity().apply(ChatMessage.class.getDeclaredConstructor(String.class, String.class)); // for AOT
        var parser = ArgumentParsers.newFor("ask").build()
                .defaultHelp(false)
                .description("ask llm");
        parser.addArgument("-c", "--config-root")
                .setDefault(System.getProperty("user.home") + "/.config/ask-cli")
                .dest("config root")
                .type(String.class)
                .help("Config root");
        parser.addArgument("-d", "--dedicated-context")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .dest("dedicated_context")
                .help("Each sentence will be in a new conversation, with initial prompt.");
        parser.addArgument("-i", "--interactive")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .dest("interactive")
                .help("Begin conversation with LLM. When used in pipe, this could split input from stdin by line.");
        parser.addArgument("prompts").nargs("*")
                .help("Your prompt. When we are in pipe, this is a system prompt.");
        try {
            var result = parser.parseArgs(args);
            var pathToCfg = Path.of(result.getString("config root"));
            var file = pathToCfg.resolve("config.json");
            if (Files.notExists(file)) {
                Files.writeString(file, GSON.toJson(new Config(null, null, null, null)));
                System.err.println("Not configured. Check " + file);
            } else {
                var cfg = new Gson().fromJson(Files.readString(file), Config.class);
                if (cfg == null) {
                    System.err.println("Not configured. Check " + file);
                } else {
                    var interactive = result.getBoolean("interactive");
                    List<String> parts = result.getList("prompts");
                    parts = parts == null ? List.of() : parts;
                    var prompt = String.join(" ", parts.toArray(String[]::new));

                    new Ask(cfg, result.getBoolean("dedicated_context")).run(interactive, prompt);
                }
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

    public record Config(String apiKey, String endpointUrl, String model, String systemPrompt) {
    }
}
