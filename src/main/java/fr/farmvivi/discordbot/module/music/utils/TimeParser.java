package fr.farmvivi.discordbot.module.music.utils;

public class TimeParser {
    public static boolean isFormatted(String time) {
        String[] numbers = time.split(":");
        for (String number : numbers)
            try {
                Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return false;
            }

        return true;
    }

    /**
     * Convert a string time to int (in milliseconds)
     *
     * @param time String time to convert
     *             Format: days:hours:minutes:seconds
     */
    public static int convertStringToInt(String time) {
        String[] numbers = time.split(":");
        return switch (numbers.length) {
            case 1 ->
                // return seconds
                    Integer.parseInt(numbers[0]);
            case 2 ->
                // return 60 * minutes + seconds
                    60 * Integer.parseInt(numbers[0]) + Integer.parseInt(numbers[1]);
            case 3 ->
                // return 60 * heures + 60 * minutes + seconds
                    60 * Integer.parseInt(numbers[0]) + 60 * Integer.parseInt(numbers[1])
                            + Integer.parseInt(numbers[2]);
            case 4 ->
                // return 24 * jours + 60 * heures + 60 * minutes + seconds
                    24 * Integer.parseInt(numbers[0]) + 60 * Integer.parseInt(numbers[1])
                            + 60 * Integer.parseInt(numbers[2]) + Integer.parseInt(numbers[3]);
            default -> 0;
        };
    }

    /**
     * Convert an int time to string
     *
     * @param time int time to convert
     * @return String time
     * Format: days:hours:minutes:seconds
     */
    public static String convertIntToString(int time) {
        int days = time / 86400;
        int hours = (time % 86400) / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time % 60;

        StringBuilder timeString = new StringBuilder();

        if (days > 0) {
            timeString.append(days).append("j ");
        }

        if (hours > 0) {
            timeString.append(hours).append("h ");
        }

        if (minutes > 0) {
            timeString.append(minutes).append("min ");
        }

        if (seconds > 0) {
            timeString.append(seconds).append("s");
        } else if (timeString.length() == 0) {
            return "0s";
        }

        return timeString.toString().trim();
    }
}
