package fr.farmvivi.animecity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Configuration {
    public String jdaToken;
    public String spotifyId;
    public String spotifySecret;
    private JsonObject jsonConfiguration;

    public Configuration() {
        try {
            this.loadConfiguration("." + File.separator + "config.json");
        } catch (IOException e) {
            Bot.logger.error("Can't load config file", e);
        }
    }

    @SuppressWarnings("deprecation")
    public void loadConfiguration(String path) throws IOException {
        Bot.logger.info("Configuration file is: " + path);
        File configurationFile = new File(path);

        if (!configurationFile.exists()) {
            Bot.logger.error("Configuration file don't exist!");
            System.exit(1);
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configurationFile),
                StandardCharsets.UTF_8)) {
            this.jsonConfiguration = new JsonParser().parse(reader).getAsJsonObject();
        }

        if (!validateJson(jsonConfiguration)) {
            Bot.logger.error("Configuration file isn't valid! Please just modify the default configuration file!");
            System.exit(1);
            return;
        }

        this.jdaToken = jsonConfiguration.get("jda-token").getAsString();
        this.spotifyId = jsonConfiguration.get("spotify-id").getAsString();
        this.spotifySecret = jsonConfiguration.get("spotify-token").getAsString();
    }

    public JsonObject getJsonConfiguration() {
        return this.jsonConfiguration;
    }

    public boolean validateJson(JsonObject object) {
        boolean flag = true;

        if (!object.has("jda-token"))
            flag = false;
        if (!object.has("spotify-id"))
            flag = false;
        if (!object.has("spotify-token"))
            flag = false;

        return flag;
    }
}
