package fr.farmvivi.animecity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Configuration {
    public String jdaToken;
    public String spotifyId;
    public String spotifySecret;
    public String cmdPrefix;
    public List<Long> cmdAdmins;
    public boolean radioEnabled;
    public long radioGuildID;
    public long radioChannelID;
    public String radioPlaylistURL;
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
        this.cmdPrefix = jsonConfiguration.get("cmd-prefix").getAsString();
        this.cmdAdmins = new ArrayList<>();
        final JsonArray adminsJsonArray = jsonConfiguration.get("cmd-admins").getAsJsonArray();
        for (int i = 0; i < adminsJsonArray.size(); i++)
            cmdAdmins.add(adminsJsonArray.get(i).getAsLong());
        this.radioEnabled = jsonConfiguration.get("radio-enabled").getAsBoolean();
        this.radioGuildID = jsonConfiguration.get("radio-guild-id").getAsLong();
        this.radioChannelID = jsonConfiguration.get("radio-channel-id").getAsLong();
        this.radioPlaylistURL = jsonConfiguration.get("radio-playlist-url").getAsString();
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
        if (!object.has("cmd-prefix"))
            flag = false;
        if (!object.has("cmd-admins"))
            flag = false;
        if (!object.has("radio-enabled"))
            flag = false;
        if (!object.has("radio-guild-id"))
            flag = false;
        if (!object.has("radio-channel-id"))
            flag = false;
        if (!object.has("radio-playlist-url"))
            flag = false;

        return flag;
    }
}
