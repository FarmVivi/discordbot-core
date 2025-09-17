package fr.farmvivi.discordbot.core.api.command.option;

/**
 * Represents a choice for a command option.
 * Used for autocompletion and validation.
 *
 * @param <T> the type of the choice value
 */
public record OptionChoice<T>(String name, T value) {
    
    /**
     * Creates a new string option choice.
     *
     * @param name  the name (label) of the choice
     * @param value the string value
     * @return a new option choice
     */
    public static OptionChoice<String> of(String name, String value) {
        return new OptionChoice<>(name, value);
    }
    
    /**
     * Creates a new integer option choice.
     *
     * @param name  the name (label) of the choice
     * @param value the integer value
     * @return a new option choice
     */
    public static OptionChoice<Integer> of(String name, int value) {
        return new OptionChoice<>(name, value);
    }
    
    /**
     * Creates a new double option choice.
     *
     * @param name  the name (label) of the choice
     * @param value the double value
     * @return a new option choice
     */
    public static OptionChoice<Double> of(String name, double value) {
        return new OptionChoice<>(name, value);
    }
}
