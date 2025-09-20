package fr.farmvivi.fluxcord.plugins.music.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting time and other display elements.
 */
public class FormatUtil {
    
    /**
     * Formats a duration in milliseconds to a human-readable format.
     * Examples: "3:45", "1:23:45", "LIVE"
     */
    public static String formatDuration(long durationMs) {
        if (durationMs == Long.MAX_VALUE) {
            return "LIVE";
        }
        
        if (durationMs < 0) {
            return "00:00";
        }
        
        long hours = TimeUnit.MILLISECONDS.toHours(durationMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Formats a track title to fit within specified character limits.
     */
    public static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Formats a number with comma separators.
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
    
    /**
     * Creates a progress bar string for track position.
     */
    public static String createProgressBar(long position, long duration, int length) {
        if (duration <= 0) {
            return "▬".repeat(length);
        }
        
        int progressChars = (int) ((double) position / duration * length);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            if (i == progressChars) {
                bar.append("🔘");
            } else {
                bar.append("▬");
            }
        }
        
        return bar.toString();
    }
    
    /**
     * Escapes markdown characters in text.
     */
    public static String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replaceAll("([*_`~|\\\\])", "\\\\$1");
    }
}