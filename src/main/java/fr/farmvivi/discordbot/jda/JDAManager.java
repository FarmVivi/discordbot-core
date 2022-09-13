package fr.farmvivi.discordbot.jda;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class JDAManager {
    private static final JDA jda = buildJDA();

    public static JDA getJDA() {
        return jda;
    }

    private static JDA buildJDA() {
        return JDABuilder.createDefault(Bot.getInstance().getConfiguration().jdaToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
    }
}
