package fr.farmvivi.discordbot.module.test.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.test.TestModule;

public class TestCommand extends Command {
    private final TestModule testModule;

    public TestCommand(TestModule testModule) {
        super("test", CommandCategory.OTHER, "Commande de test");

        this.adminOnly = true;

        this.testModule = testModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;

        reply.addContent("Test");

        return true;
    }
}
