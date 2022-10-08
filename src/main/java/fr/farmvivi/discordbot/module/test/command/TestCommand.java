package fr.farmvivi.discordbot.module.test.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.test.TestModule;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Map;

public class TestCommand extends Command {
    private final TestModule testModule;

    public TestCommand(TestModule testModule) {
        super("test", CommandCategory.OTHER, "Commande de test");

        this.adminOnly = true;

        this.testModule = testModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        reply.addContent("Test");

        return true;
    }
}
