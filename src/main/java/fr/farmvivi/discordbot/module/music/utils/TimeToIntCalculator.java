package fr.farmvivi.discordbot.module.music.utils;

public class TimeToIntCalculator {
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

    public static int format(String time) {
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
}
