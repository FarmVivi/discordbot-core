package fr.farmvivi.discordbot.jda;

import fr.farmvivi.discordbot.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class JDAManager {
    private static final JDA jda = buildJDA();

    public static JDA getJDA() {
        return jda;
    }

    private static JDA buildJDA() {
        return JDABuilder.createDefault(Bot.getInstance().getConfiguration().jdaToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
    }
}
