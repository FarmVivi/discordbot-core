package fr.farmvivi.fluxcord.plugins.music.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and formatting time values.
 */
public class TimeParser {
    private static final Pattern TIME_PATTERN = Pattern.compile(
        "(?:([0-9]+)h)?(?:([0-9]+)m)?(?:([0-9]+)s)?", 
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Parses a time string (e.g., "1h30m", "45s", "2m30s") to milliseconds.
     * @param input the time string
     * @return the time in milliseconds, or -1 if invalid
     */
    public static long parseTime(String input) {
        if (input == null || input.isEmpty()) {
            return -1;
        }
        
        // Try parsing as plain number (seconds)
        try {
            return Long.parseLong(input) * 1000;
        } catch (NumberFormatException ignored) {
        }
        
        // Try parsing with time units
        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase());
        if (!matcher.matches()) {
            return -1;
        }
        
        long totalMs = 0;
        
        String hours = matcher.group(1);
        String minutes = matcher.group(2);
        String seconds = matcher.group(3);
        
        if (hours != null) {
            totalMs += Long.parseLong(hours) * 3600000;
        }
        if (minutes != null) {
            totalMs += Long.parseLong(minutes) * 60000;
        }
        if (seconds != null) {
            totalMs += Long.parseLong(seconds) * 1000;
        }
        
        return totalMs > 0 ? totalMs : -1;
    }
    
    /**
     * Formats milliseconds to a readable time string.
     * @param milliseconds the time in milliseconds
     * @return formatted time string (e.g., "1:30:45" or "45:30")
     */
    public static String formatTime(long milliseconds) {
        if (milliseconds < 0) {
            return "∞";
        }
        
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds %= 60;
        minutes %= 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Formats milliseconds to a short time string with units.
     * @param milliseconds the time in milliseconds
     * @return formatted time string (e.g., "1h 30m", "45s")
     */
    public static String formatTimeShort(long milliseconds) {
        if (milliseconds < 0) {
            return "∞";
        }
        
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds %= 60;
        minutes %= 60;
        
        StringBuilder result = new StringBuilder();
        
        if (hours > 0) {
            result.append(hours).append("h ");
        }
        if (minutes > 0) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append("s");
        }
        
        return result.toString().trim();
    }
}