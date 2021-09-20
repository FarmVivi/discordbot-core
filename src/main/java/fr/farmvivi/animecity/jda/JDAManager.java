package fr.farmvivi.animecity.jda;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class JDAManager {
    public static final String BOT_TOKEN = "ODg5MjQwMTIxNTEyMTk0MDQ4.YUeXZg.7XEk-LE0_7PK1xtrGx1vNlPqVy8";

    private static ShardManager shardManager = buildShard();

    public static ShardManager getShardManager() {
        return shardManager;
    }

    private static ShardManager buildShard() {
        try {
            return DefaultShardManagerBuilder.createDefault(BOT_TOKEN).build();
        } catch (LoginException e) {
            System.out.println("Impossible de build le Shard !");
            e.printStackTrace();
        }
        return null;
    }
}
