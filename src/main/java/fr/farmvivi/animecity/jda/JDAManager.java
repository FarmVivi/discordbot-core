package fr.farmvivi.animecity.jda;

import javax.security.auth.login.LoginException;

import fr.farmvivi.animecity.Bot;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class JDAManager {
    private static final ShardManager shardManager = buildShard();

    public static ShardManager getShardManager() {
        return shardManager;
    }

    private static ShardManager buildShard() {
        try {
            return DefaultShardManagerBuilder.createDefault(Bot.getInstance().getConfiguration().jdaToken).build();
        } catch (LoginException e) {
            Bot.logger.error("Impossible de build le Shard !", e);
        }
        return null;
    }
}
