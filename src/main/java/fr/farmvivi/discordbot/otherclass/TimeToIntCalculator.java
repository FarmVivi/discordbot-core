package fr.farmvivi.discordbot.otherclass;

public class TimeToIntCalculator {
    public static boolean isFormated(String time) {
        final String[] numbers = time.split(":");
        for (final String number : numbers)
            try {
                Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return false;
            }

        return true;
    }

    public static int format(String time) {
        final String[] numbers = time.split(":");
        switch (numbers.length) {
            case 1:
                // return seconds
                return Integer.valueOf(numbers[0]);
            case 2:
                // return 60 * minutes + seconds
                return 60 * Integer.valueOf(numbers[0]) + Integer.valueOf(numbers[1]);
            case 3:
                // return 60 * heures + 60 * minutes + seconds
                return 60 * Integer.valueOf(numbers[0]) + 60 * Integer.valueOf(numbers[1])
                        + Integer.valueOf(numbers[2]);
            case 4:
                // return 24 * jours + 60 * heures + 60 * minutes + seconds
                return 24 * Integer.valueOf(numbers[0]) + 60 * Integer.valueOf(numbers[1])
                        + 60 * Integer.valueOf(numbers[2]) + Integer.valueOf(numbers[3]);
            default:
                return 0;
        }
    }
}
