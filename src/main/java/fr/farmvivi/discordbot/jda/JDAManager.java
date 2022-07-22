package fr.farmvivi.discordbot.jda;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class JDAManager {
    public static final Logger logger = LoggerFactory.getLogger(JDAManager.class);

    private static final ShardManager shardManager = buildShard();

    public static ShardManager getShardManager() {
        return shardManager;
    }

    private static ShardManager buildShard() {
        try {
            return DefaultShardManagerBuilder.createDefault(Bot.getInstance().getConfiguration().jdaToken)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        } catch (LoginException e) {
            logger.error("Cannot build shard !", e);
        }

        return null;
    }
}
