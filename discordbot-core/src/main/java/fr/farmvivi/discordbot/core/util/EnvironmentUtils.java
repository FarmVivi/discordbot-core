package fr.farmvivi.discordbot.core.util;

public class EnvironmentUtils {
    private static final String ENV_PREFIX = "DISCORD_";

    /**
     * Gets a string value from environment variables.
     *
     * @param key the key without prefix
     * @return the value or null if not found
     */
    public static String getEnv(String key) {
        String envKey = ENV_PREFIX + key.toUpperCase().replace(".", "_");
        return System.getenv(envKey);
    }

    /**
     * Gets a list of string values from environment variables.
     *
     * @param key the key without prefix
     * @return the values or empty list if not found
     */
    public static String[] getEnvValues(String key) {
        String value = getEnv(key);
        if (value != null) {
            return value.split(":");
        }
        return new String[0];
    }
}