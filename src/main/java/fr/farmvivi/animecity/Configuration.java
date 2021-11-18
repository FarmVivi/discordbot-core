package fr.farmvivi.animecity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configuration {
    public String jdaToken;
    public String spotifyId;
    public String spotifySecret;
    public String cmdPrefix;
    public List<Long> cmdAdmins;

    public Configuration() {
        try {
            this.loadConfiguration();
        } catch (Exception e) {
            Bot.logger.error("Error loading configuration", e);
        }
    }

    public void loadConfiguration() throws Exception {
        if (!validateConfiguration()) {
            Bot.logger.error("Configuration isn't valid! Please just modify the default configuration !");
            System.exit(1);
            return;
        }

        this.jdaToken = System.getenv("DISCORD_TOKEN");
        this.spotifyId = System.getenv("SPOTIFY_ID");
        this.spotifySecret = System.getenv("SPOTIFY_TOKEN");
        this.cmdPrefix = System.getenv("BOT_CMD_PREFIX");
        this.cmdAdmins = new ArrayList<>();
        for (String admin : Arrays.asList(System.getenv("BOT_CMD_ADMINS").split(";")))
            cmdAdmins.add(Long.parseLong(admin));
    }

    public boolean validateConfiguration() {
        if (System.getenv("DISCORD_TOKEN") == null)
            return false;
        if (System.getenv("SPOTIFY_ID") == null)
            return false;
        if (System.getenv("SPOTIFY_TOKEN") == null)
            return false;
        if (System.getenv("BOT_CMD_PREFIX") == null)
            return false;
        if (System.getenv("BOT_CMD_ADMINS") == null)
            return false;

        return true;
    }
}
