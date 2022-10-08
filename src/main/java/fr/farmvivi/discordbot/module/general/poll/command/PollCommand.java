package fr.farmvivi.discordbot.module.general.poll.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.general.poll.SimplePool;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PollCommand extends Command {
    public PollCommand() {
        super("poll", CommandCategory.UTILS, "Faire un sondage");

        OptionData questionOption = new OptionData(OptionType.STRING, "question", "Question du sondage", true);
        OptionData response1Option = new OptionData(OptionType.STRING, "réponse1", "Option 1", true);
        OptionData response2Option = new OptionData(OptionType.STRING, "réponse2", "Option 2", true);
        OptionData respondentOption = new OptionData(OptionType.ROLE, "répondants", "Les personnes qui peuvent répondre au sondage", false);
        OptionData timeoutOption = new OptionData(OptionType.INTEGER, "durée", "La durée du sondage en secondes", false);
        timeoutOption.setMinValue(30);
        OptionData response3Option = new OptionData(OptionType.STRING, "réponse3", "Option 3", false);
        OptionData response4Option = new OptionData(OptionType.STRING, "réponse4", "Option 4", false);
        OptionData response5Option = new OptionData(OptionType.STRING, "réponse5", "Option 5", false);
        OptionData response6Option = new OptionData(OptionType.STRING, "réponse6", "Option 6", false);
        OptionData response7Option = new OptionData(OptionType.STRING, "réponse7", "Option 7", false);
        OptionData response8Option = new OptionData(OptionType.STRING, "réponse8", "Option 8", false);
        OptionData response9Option = new OptionData(OptionType.STRING, "réponse9", "Option 9", false);
        OptionData response10Option = new OptionData(OptionType.STRING, "réponse10", "Option 10", false);

        this.setArgs(new OptionData[]{questionOption, response1Option, response2Option, respondentOption, timeoutOption, response3Option, response4Option, response5Option, response6Option, response7Option, response8Option, response9Option, response10Option});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        GuildMessageChannel channel;
        try {
            channel = event.getChannel().asGuildMessageChannel();
        } catch (IllegalStateException e) {
            reply.setContent("Vous devez être dans un salon d'une guilde !");
            return false;
        }

        String question = args.get("question").getAsString();
        List<String> responses = new LinkedList<>();
        responses.add(args.get("réponse1").getAsString());
        responses.add(args.get("réponse2").getAsString());
        if (args.containsKey("réponse3"))
            responses.add(args.get("réponse3").getAsString());
        if (args.containsKey("réponse4"))
            responses.add(args.get("réponse4").getAsString());
        if (args.containsKey("réponse5"))
            responses.add(args.get("réponse5").getAsString());
        if (args.containsKey("réponse6"))
            responses.add(args.get("réponse6").getAsString());
        if (args.containsKey("réponse7"))
            responses.add(args.get("réponse7").getAsString());
        if (args.containsKey("réponse8"))
            responses.add(args.get("réponse8").getAsString());
        if (args.containsKey("réponse9"))
            responses.add(args.get("réponse9").getAsString());
        if (args.containsKey("réponse10"))
            responses.add(args.get("réponse10").getAsString());

        long timeout = -1;
        if (args.containsKey("durée"))
            timeout = args.get("durée").getAsLong();
        SimplePool pool;
        if (args.containsKey("répondants")) {
            Role role = args.get("répondants").getAsRole();
            pool = new SimplePool(role, timeout, question, responses.toArray(new String[0]));
        } else {
            pool = new SimplePool(timeout, question, responses.toArray(new String[0]));
        }
        pool.sendPoll(channel);

        return true;
    }
}
