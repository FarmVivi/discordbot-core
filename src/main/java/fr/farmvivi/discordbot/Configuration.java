package fr.farmvivi.discordbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private static final String ENV_PREFIX = "DISCORD_";

    public String jdaToken;
    public String countryCode;
    public String cmdPrefix;
    public List<Long> cmdAdmins;
    public String radioPath;
    public List<String> features;

    private JsonObject jsonConfiguration;

    public Configuration() {
        try {
            this.loadJsonConfiguration("." + File.separator + "config.json");
        } catch (IOException e) {
            Bot.logger.warn("Failed to load json configuration file", e);
        }
        try {
            this.loadDefaultConfiguration();
        } catch (ValueNotFoundException e) {
            Bot.logger.error("Failed to load default configuration !", e);
            System.exit(5);
        }
    }

    @SuppressWarnings("deprecation")
    private void loadJsonConfiguration(String path) throws IOException {
        Bot.logger.info("Configuration file is: " + path);

        File configurationFile = new File(path);

        if (!configurationFile.exists()) {
            Bot.logger.warn("Configuration file don't exist");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configurationFile),
                StandardCharsets.UTF_8)) {
            this.jsonConfiguration = new JsonParser().parse(reader).getAsJsonObject();
        }
    }

    private void loadDefaultConfiguration() throws ValueNotFoundException {
        this.jdaToken = getValue("TOKEN");
        this.countryCode = getValue("COUNTRY");
        this.cmdPrefix = getValue("CMD_PREFIX");
        this.cmdAdmins = new ArrayList<>();
        for (String admin : getValues("CMD_ADMINS"))
            cmdAdmins.add(Long.parseLong(admin));
        this.radioPath = getValue("RADIO_PATH");
        this.features = List.of(getValues("FEATURES"));
    }

    public String getValue(String key) throws ValueNotFoundException {
        String envKey = ENV_PREFIX + key.toUpperCase().replace("-", "_");
        if (System.getenv(envKey) != null) {
            return System.getenv(envKey);
        }

        if (jsonConfiguration != null && jsonConfiguration.has(key)) {
            return jsonConfiguration.get(key).getAsString();
        }

        throw new ValueNotFoundException(
                "Environment key '" + envKey + "' not found in environment variables or key '" + key
                        + "' not found in configuration file");
    }

    public String[] getValues(String key) throws ValueNotFoundException {
        String envKey = ENV_PREFIX + key.toUpperCase().replace("-", "_");
        if (System.getenv(envKey) != null) {
            return System.getenv(envKey).split(";");
        }

        if (jsonConfiguration != null && jsonConfiguration.has(key)) {
            JsonArray jsonArray = jsonConfiguration.get(key).getAsJsonArray();
            String[] values = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                values[i] = jsonArray.get(i).getAsString();
            }
            return values;
        }

        throw new ValueNotFoundException(
                "Environment key '" + envKey + "' not found in environment variables or key '" + key
                        + "' not found in configuration file");
    }

    public static class ValueNotFoundException extends Exception {
        public ValueNotFoundException(String message) {
            super(message);
        }
    }
}
