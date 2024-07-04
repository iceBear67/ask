package io.ib67.ask;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("slf4j.internal.verbosity", "ERROR"); // 吵得要死
        var userProvidedCfgRt = System.getenv("CONFIG_ROOT");
        var pathToCfg = Path.of(
                userProvidedCfgRt == null ?
                        System.getProperty("user.home") + "/.config/ask-cli"
                        : userProvidedCfgRt
        );
        var file = pathToCfg.resolve("config.json");
        if (Files.notExists(file)) {
            Files.writeString(file, new Gson().toJson(new Config(null, null, null, null)));
            System.err.println("Not configured. Check ~/.config/ask-cli/config.json");
        } else {
            var cfg = new Gson().fromJson(Files.readString(file), Config.class);
            if (cfg == null) {
                System.err.println("Not configured. Check ~/.config/ask-cli/config.json");
            } else {
                var interactive = System.in.available() == 0;
                if (args.length != 0 && args[0].equals("-o")) {
                    interactive = false;
                    args = Arrays.copyOfRange(args, 1, args.length);
                }
                new Ask(cfg.apiKey, cfg.endpointUrl, cfg.model, cfg.systemPrompt).run(interactive, args);
            }

        }
    }

    static class Config {
        private final String apiKey;
        private final String endpointUrl;
        private final String model;
        private final String systemPrompt;

        Config(String apiKey, String endpointUrl, String model, String systemPrompt) {
            this.apiKey = apiKey;
            this.endpointUrl = endpointUrl;
            this.model = model;
            this.systemPrompt = systemPrompt;
        }
    }
}
