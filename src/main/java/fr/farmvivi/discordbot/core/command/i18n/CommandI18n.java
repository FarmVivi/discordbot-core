package fr.farmvivi.discordbot.core.command.i18n;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.command.Command;

import java.util.Locale;

/**
 * Utility class for command internationalization.
 * Provides methods for translating command-related messages.
 */
public class CommandI18n {

    private final LanguageManager languageManager;

    /**
     * Creates a new command i18n utility.
     *
     * @param languageManager the language manager
     */
    public CommandI18n(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    /**
     * Gets a translated message with the default locale.
     *
     * @param key  the translation key
     * @param args the arguments for placeholder replacement
     * @return the translated message
     */
    public String getMessage(String key, Object... args) {
        return languageManager.getString(key, args);
    }

    /**
     * Gets a translated message with a specific locale.
     *
     * @param locale the locale
     * @param key    the translation key
     * @param args   the arguments for placeholder replacement
     * @return the translated message
     */
    public String getMessage(Locale locale, String key, Object... args) {
        return languageManager.getString(locale, key, args);
    }

    /**
     * Gets the translated name of a command.
     *
     * @param command the command
     * @return the translated name, or the command name if no translation is available
     */
    public String getCommandName(Command command) {
        String key = command.getTranslationKey() + ".name";
        String translated = getMessage(key);
        return translated.equals(key) ? command.getName() : translated;
    }

    /**
     * Gets the translated name of a command in a specific locale.
     *
     * @param command the command
     * @param locale  the locale
     * @return the translated name, or the command name if no translation is available
     */
    public String getCommandName(Command command, Locale locale) {
        String key = command.getTranslationKey() + ".name";
        String translated = getMessage(locale, key);
        return translated.equals(key) ? command.getName() : translated;
    }

    /**
     * Gets the translated description of a command.
     *
     * @param command the command
     * @return the translated description, or the command description if no translation is available
     */
    public String getCommandDescription(Command command) {
        String key = command.getTranslationKey() + ".description";
        String translated = getMessage(key);
        return translated.equals(key) ? command.getDescription() : translated;
    }

    /**
     * Gets the translated description of a command in a specific locale.
     *
     * @param command the command
     * @param locale  the locale
     * @return the translated description, or the command description if no translation is available
     */
    public String getCommandDescription(Command command, Locale locale) {
        String key = command.getTranslationKey() + ".description";
        String translated = getMessage(locale, key);
        return translated.equals(key) ? command.getDescription() : translated;
    }

    /**
     * Gets the translated category of a command.
     *
     * @param command the command
     * @return the translated category, or the command category if no translation is available
     */
    public String getCommandCategory(Command command) {
        String key = CommandTranslationKeys.PREFIX + "category." + command.getCategory().toLowerCase();
        String translated = getMessage(key);
        return translated.equals(key) ? command.getCategory() : translated;
    }

    /**
     * Gets the translated category of a command in a specific locale.
     *
     * @param command the command
     * @param locale  the locale
     * @return the translated category, or the command category if no translation is available
     */
    public String getCommandCategory(Command command, Locale locale) {
        String key = CommandTranslationKeys.PREFIX + "category." + command.getCategory().toLowerCase();
        String translated = getMessage(locale, key);
        return translated.equals(key) ? command.getCategory() : translated;
    }

    /**
     * Gets the command not found error message.
     *
     * @param commandName the command name
     * @return the error message
     */
    public String getCommandNotFoundError(String commandName) {
        return getMessage(CommandTranslationKeys.Error.COMMAND_NOT_FOUND, commandName);
    }

    /**
     * Gets the command not found error message in a specific locale.
     *
     * @param commandName the command name
     * @param locale     the locale
     * @return the error message
     */
    public String getCommandNotFoundError(String commandName, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Error.COMMAND_NOT_FOUND, commandName);
    }

    /**
     * Gets the command execution failed error message.
     *
     * @param command     the command
     * @param errorMessage the error message
     * @return the error message
     */
    public String getExecutionFailedError(Command command, String errorMessage) {
        return getMessage(CommandTranslationKeys.Error.EXECUTION_FAILED, command.getName(), errorMessage);
    }

    /**
     * Gets the command execution failed error message in a specific locale.
     *
     * @param command     the command
     * @param errorMessage the error message
     * @param locale      the locale
     * @return the error message
     */
    public String getExecutionFailedError(Command command, String errorMessage, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Error.EXECUTION_FAILED, command.getName(), errorMessage);
    }

    /**
     * Gets the guild-only command error message.
     *
     * @param command the command
     * @return the error message
     */
    public String getGuildOnlyError(Command command) {
        return getMessage(CommandTranslationKeys.Error.GUILD_ONLY, command.getName());
    }

    /**
     * Gets the guild-only command error message in a specific locale.
     *
     * @param command the command
     * @param locale  the locale
     * @return the error message
     */
    public String getGuildOnlyError(Command command, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Error.GUILD_ONLY, command.getName());
    }

    /**
     * Gets the no permission error message.
     *
     * @param command    the command
     * @param permission the required permission
     * @return the error message
     */
    public String getNoPermissionError(Command command, String permission) {
        return getMessage(CommandTranslationKeys.Error.NO_PERMISSION, command.getName(), permission);
    }

    /**
     * Gets the no permission error message in a specific locale.
     *
     * @param command    the command
     * @param permission the required permission
     * @param locale     the locale
     * @return the error message
     */
    public String getNoPermissionError(Command command, String permission, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Error.NO_PERMISSION, command.getName(), permission);
    }

    /**
     * Gets the missing option error message.
     *
     * @param optionName the option name
     * @param command    the command
     * @return the error message
     */
    public String getMissingOptionError(String optionName, Command command) {
        return getMessage(CommandTranslationKeys.Error.MISSING_OPTION, optionName, command.getName());
    }

    /**
     * Gets the missing option error message in a specific locale.
     *
     * @param optionName the option name
     * @param command    the command
     * @param locale     the locale
     * @return the error message
     */
    public String getMissingOptionError(String optionName, Command command, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Error.MISSING_OPTION, optionName, command.getName());
    }

    /**
     * Gets the invalid option error message.
     *
     * @param optionName    the option name
     * @param providedValue the provided value
     * @param command       the command
     * @return the error message
     */
    public String getInvalidOptionError(String optionName, String providedValue, Command command) {
        return getMessage(CommandTranslationKeys.Error.INVALID_OPTION, optionName, providedValue, command.getName());
    }

    /**
     * Gets the invalid option error message in a specific locale.
     *
     * @param optionName    the option name
     * @param providedValue the provided value
     * @param command       the command
     * @param locale        the locale
     * @return the error message
     */
    public String getInvalidOptionError(String optionName, String providedValue, Command command, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Error.INVALID_OPTION, optionName, providedValue, command.getName());
    }

    /**
     * Gets the cooldown error message.
     *
     * @param command          the command
     * @param remainingSeconds the remaining seconds
     * @return the error message
     */
    public String getCooldownError(Command command, int remainingSeconds) {
        return getMessage(CommandTranslationKeys.Error.COOLDOWN, command.getName(), remainingSeconds);
    }

    /**
     * Gets the cooldown error message in a specific locale.
     *
     * @param command          the command
     * @param remainingSeconds the remaining seconds
     * @param locale           the locale
     * @return the error message
     */
    public String getCooldownError(Command command, int remainingSeconds, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Error.COOLDOWN, command.getName(), remainingSeconds);
    }

    /**
     * Gets the command disabled error message.
     *
     * @param command the command
     * @return the error message
     */
    public String getCommandDisabledError(Command command) {
        return getMessage(CommandTranslationKeys.Error.DISABLED, command.getName());
    }

    /**
     * Gets the command disabled error message in a specific locale.
     *
     * @param command the command
     * @param locale  the locale
     * @return the error message
     */
    public String getCommandDisabledError(Command command, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Error.DISABLED, command.getName());
    }

    /**
     * Gets the command help title.
     *
     * @param command the command
     * @return the help title
     */
    public String getHelpTitle(Command command) {
        return getMessage(CommandTranslationKeys.Help.TITLE, command.getName());
    }

    /**
     * Gets the command help title in a specific locale.
     *
     * @param command the command
     * @param locale  the locale
     * @return the help title
     */
    public String getHelpTitle(Command command, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Help.TITLE, command.getName());
    }

    /**
     * Gets the command usage message.
     *
     * @param command    the command
     * @param fullUsage  the full usage syntax
     * @return the usage message
     */
    public String getUsage(Command command, String fullUsage) {
        return getMessage(CommandTranslationKeys.Help.USAGE, command.getName(), fullUsage);
    }

    /**
     * Gets the command usage message in a specific locale.
     *
     * @param command    the command
     * @param fullUsage  the full usage syntax
     * @param locale     the locale
     * @return the usage message
     */
    public String getUsage(Command command, String fullUsage, Locale locale) {
        return getMessage(locale, CommandTranslationKeys.Help.USAGE, command.getName(), fullUsage);
    }
}
