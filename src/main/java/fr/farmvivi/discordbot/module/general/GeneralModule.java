package fr.farmvivi.discordbot.module.general;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.general.poll.PollManager;
import fr.farmvivi.discordbot.module.general.poll.command.PollCommand;

public class GeneralModule extends Module {
    private final Bot bot;
    private final PollManager pollManager;

    public GeneralModule(Bot bot) {
        super(Modules.GENERAL);

        this.bot = bot;
        this.pollManager = new PollManager();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.registerCommand(module, new PollCommand());
    }

    @Override
    public void onPostEnable() {
        super.onPostEnable();

        logger.info("Registering event listener...");

        JDAManager.getJDA().addEventListener(pollManager);
    }

    @Override
    public void onPreDisable() {
        super.onPreDisable();

        logger.info("Unregistering event listener...");

        JDAManager.getJDA().removeEventListener(pollManager);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        CommandsModule commandsModule = (CommandsModule) bot.getModulesManager().getModule(Modules.COMMANDS);

        commandsModule.unregisterCommands(module);
    }

    public PollManager getPollManager() {
        return pollManager;
    }
}
