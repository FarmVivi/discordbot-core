package fr.farmvivi.discordbot.jda;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class JDAManager {
    public static final Logger logger = LoggerFactory.getLogger(JDAManager.class);

    private static final JDA jda = buildJDA();

    public static JDA getJDA() {
        return jda;
    }

    private static JDA buildJDA() {
        try {
            return JDABuilder.createDefault(Bot.getInstance().getConfiguration().jdaToken)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        } catch (LoginException e) {
            logger.error("Cannot build JDA !", e);
        }

        return null;
    }
}
