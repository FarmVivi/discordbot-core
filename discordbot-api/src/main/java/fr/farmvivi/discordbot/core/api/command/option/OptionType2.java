package fr.farmvivi.discordbot.core.api.command.option;


import net.dv8tion.jda.api.interactions.commands.OptionType;

/**
 * Types of command options supported by the command system.
 * These types correspond to the option types supported by Discord slash commands.
 */
public enum OptionType2 {
    /**
     * String option type (text input).
     */
    STRING(OptionType.STRING),

    /**
     * Integer option type (whole numbers).
     */
    INTEGER(OptionType.INTEGER),

    /**
     * Boolean option type (true/false).
     */
    BOOLEAN(OptionType.BOOLEAN),

    /**
     * User option type (Discord user).
     */
    USER(OptionType.USER),

    /**
     * Channel option type (Discord channel).
     */
    CHANNEL(OptionType.CHANNEL),

    /**
     * Role option type (Discord role).
     */
    ROLE(OptionType.ROLE),

    /**
     * Mentionable option type (user or role).
     */
    MENTIONABLE(OptionType.MENTIONABLE),

    /**
     * Decimal number option type.
     */
    NUMBER(OptionType.NUMBER),

    /**
     * File attachment option type.
     */
    ATTACHMENT(OptionType.ATTACHMENT);

    private final OptionType jdaType;

    OptionType2(OptionType jdaType) {
        this.jdaType = jdaType;
    }

    /**
     * Gets the corresponding JDA option type.
     *
     * @return the JDA option type
     */
    public OptionType getJdaType() {
        return jdaType;
    }

    /**
     * Converts a JDA option type to our option type.
     *
     * @param jdaType the JDA option type
     * @return the corresponding option type
     */
    public static OptionType2 fromJdaType(OptionType jdaType) {
        for (OptionType2 type : values()) {
            if (type.getJdaType() == jdaType) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported JDA option type: " + jdaType);
    }
}
