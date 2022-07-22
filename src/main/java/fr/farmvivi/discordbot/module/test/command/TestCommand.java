package fr.farmvivi.discordbot.module.test.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.test.TestModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TestCommand extends Command {
    private final TestModule testModule;

    public TestCommand(TestModule testModule) {
        this.name = "test";
        this.category = CommandCategory.OTHER;
        this.description = "Commande de test";
        this.adminOnly = true;

        this.testModule = testModule;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String content) {
        if (!super.execute(event, content))
            return false;

        TextChannel textChannel = event.getChannel().asTextChannel();
        Guild guild = textChannel.getGuild();

        textChannel.sendMessage("Test").queue();

        return true;
    }
}
