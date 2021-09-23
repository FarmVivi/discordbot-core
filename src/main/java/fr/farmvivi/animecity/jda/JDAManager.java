package fr.farmvivi.animecity.jda;

import javax.security.auth.login.LoginException;

import fr.farmvivi.animecity.Bot;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class JDAManager {
    public static final String BOT_TOKEN_PROD = "ODg5MjQwMTIxNTEyMTk0MDQ4.YUeXZg.7XEk-LE0_7PK1xtrGx1vNlPqVy8";
    public static final String BOT_TOKEN_DEV = "ODkwNDgxMTk2MTAwOTExMTM1.YUwbPQ.KTJ3usPstSalNW6HJfXzQ-pJYbY";

    private static ShardManager shardManager = buildShard();

    public static ShardManager getShardManager() {
        return shardManager;
    }

    private static ShardManager buildShard() {
        try {
            if (Bot.production)
                return DefaultShardManagerBuilder.createDefault(BOT_TOKEN_PROD).build();
            else
                return DefaultShardManagerBuilder.createDefault(BOT_TOKEN_DEV).build();
        } catch (LoginException e) {
            Bot.logger.info("Impossible de build le Shard !");
            e.printStackTrace();
        }
        return null;
    }
}
