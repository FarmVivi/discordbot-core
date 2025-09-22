package fr.farmvivi.fluxcord.plugins.music;

import fr.farmvivi.fluxcord.api.language.PluginLanguageAdapter;
import fr.farmvivi.fluxcord.plugins.music.player.MusicPlayer;
import fr.farmvivi.fluxcord.plugins.music.ui.MusicPlayerMessage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * Handles button interactions for the music player.
 */
public class ButtonHandler {
    private final MusicPlugin plugin;

    public ButtonHandler(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void handleButton(ButtonInteractionEvent event, MusicPlayerMessage.ButtonInfo info) {
        Guild guild = event.getGuild();
        if (guild == null || !guild.getId().equals(info.guildId)) {
            if (!event.isAcknowledged()) {
                event.reply(plugin.getPluginLanguageManager().getString("music.error.wrong_guild"))
                        .setEphemeral(true)
                        .queue();
            }
            return;
        }

        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            if (!event.isAcknowledged()) {
                event.reply(plugin.getPluginLanguageManager().getString("music.error.not_in_voice"))
                        .setEphemeral(true)
                        .queue();
            }
            return;
        }

        MusicPlayer player = plugin.getMusicManager().getPlayer(guild);

        // Handle action with optional value (e.g., "volume:+10")
        String action = info.action;
        String value = null;
        if (action.contains(":")) {
            String[] parts = action.split(":", 2);
            action = parts[0];
            value = parts[1];
        }

    switch (action) {
            case "add":
                // This would open a modal or send instructions
        if (!event.isAcknowledged()) {
            event.reply(plugin.getPluginLanguageManager().getString("music.button.add_help"))
                .setEphemeral(true)
                .queue();
        }
                break;

            case "pause":
                if (!hasPermission(member, "music.play")) {
                    replyNoPermission(event);
                    return;
                }
                event.deferEdit().queue();
                player.togglePause();
                break;

            case "skip":
                if (!hasPermission(member, "music.skip")) {
                    replyNoPermission(event);
                    return;
                }
                event.deferEdit().queue();
                player.skip();
                break;

            case "stop":
                if (!hasPermission(member, "music.play")) {
                    replyNoPermission(event);
                    return;
                }
                event.deferEdit().queue();
                player.stopAndLeave();
                break;

            case "clear":
                if (!hasPermission(member, "music.admin")) {
                    replyNoPermission(event);
                    return;
                }
                event.deferEdit().queue();
                player.clearQueue();
                break;

            case "loop":
                event.deferEdit().queue();
                player.toggleLoop();
                break;

            case "loopqueue":
                event.deferEdit().queue();
                player.toggleLoopQueue();
                break;

            case "shuffle":
                event.deferEdit().queue();
                player.toggleShuffle();
                break;

            case "volume":
                if (!hasPermission(member, "music.volume")) {
                    replyNoPermission(event);
                    return;
                }
                event.deferEdit().queue();
                if (value != null) {
                    try {
                        int change = Integer.parseInt(value);
                        player.changeVolume(change);
                    } catch (NumberFormatException ignored) {
                    }
                }
                break;

            case "mute":
                if (!hasPermission(member, "music.volume")) {
                    replyNoPermission(event);
                    return;
                }
                event.deferEdit().queue();
                player.toggleMute();
                break;

            default:
        if (!event.isAcknowledged()) {
            event.reply(plugin.getPluginLanguageManager().getString("music.error.unknown_action"))
                .setEphemeral(true)
                .queue();
        }
                break;
        }
    }

    private boolean hasPermission(Member member, String permission) {
        String userId = member.getId();
        String guildId = member.getGuild().getId();
        // Permission nodes are registered as pluginName.node
        String perm = plugin.getName().toLowerCase() + "." + permission.substring(permission.indexOf('.') + 1);
        return plugin.getPluginPermissionManager().hasPermission(userId, guildId, perm)
                || plugin.getPluginPermissionManager().hasPermission(userId, perm);
    }

    private void replyNoPermission(ButtonInteractionEvent event) {
        PluginLanguageAdapter lm = plugin.getPluginLanguageManager();
        if (!event.isAcknowledged()) {
            event.reply(lm.getString("music.error.no_permission")).setEphemeral(true).queue();
        }
    }
}