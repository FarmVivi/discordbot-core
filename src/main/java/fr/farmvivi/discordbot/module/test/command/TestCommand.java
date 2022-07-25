package fr.farmvivi.discordbot.module.test.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.test.TestModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class TestCommand extends Command {
    private final TestModule testModule;

    public TestCommand(TestModule testModule) {
        super("test", CommandCategory.OTHER, "Commande de test");

        this.adminOnly = true;

        this.testModule = testModule;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = textChannel.getGuild();

        textChannel.sendMessage("Test").queue();

        return true;
    }
}
