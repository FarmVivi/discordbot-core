package fr.farmvivi.fluxcord.plugins.music;

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
            event.reply(plugin.getPluginLanguageAdapter().getString("music.error.wrong_guild"))
                 .setEphemeral(true)
                 .queue();
            return;
        }
        
        Member member = event.getMember();
        if (member == null || member.getVoiceState().getChannel() == null) {
            event.reply(plugin.getPluginLanguageAdapter().getString(guild, "music.error.not_in_voice"))
                 .setEphemeral(true)
                 .queue();
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
                event.reply(plugin.getPluginLanguageAdapter().getString(guild, "music.button.add_help"))
                     .setEphemeral(true)
                     .queue();
                break;
                
            case "pause":
                if (!hasPermission(member, "music.play")) {
                    replyNoPermission(event);
                    return;
                }
                player.setPaused(!player.isPaused());
                event.deferEdit().queue();
                break;
                
            case "skip":
                if (!hasPermission(member, "music.skip")) {
                    replyNoPermission(event);
                    return;
                }
                player.skipTrack();
                event.deferEdit().queue();
                break;
                
            case "stop":
                if (!hasPermission(member, "music.play")) {
                    replyNoPermission(event);
                    return;
                }
                player.stop();
                guild.getAudioManager().closeAudioConnection();
                event.deferEdit().queue();
                break;
                
            case "clear":
                if (!hasPermission(member, "music.admin")) {
                    replyNoPermission(event);
                    return;
                }
                player.getTrackScheduler().clear();
                event.deferEdit().queue();
                break;
                
            case "loop":
                player.getTrackScheduler().setLoopMode(!player.getTrackScheduler().isLoopMode());
                if (player.getTrackScheduler().isLoopMode()) {
                    player.getTrackScheduler().setLoopQueueMode(false);
                }
                event.deferEdit().queue();
                break;
                
            case "loopqueue":
                player.getTrackScheduler().setLoopQueueMode(!player.getTrackScheduler().isLoopQueueMode());
                if (player.getTrackScheduler().isLoopQueueMode()) {
                    player.getTrackScheduler().setLoopMode(false);
                }
                event.deferEdit().queue();
                break;
                
            case "shuffle":
                player.getTrackScheduler().setShuffleMode(!player.getTrackScheduler().isShuffleMode());
                event.deferEdit().queue();
                break;
                
            case "volume":
                if (!hasPermission(member, "music.volume")) {
                    replyNoPermission(event);
                    return;
                }
                if (value != null) {
                    try {
                        int change = Integer.parseInt(value);
                        int newVolume = Math.max(0, Math.min(100, player.getVolume() + change));
                        player.setVolume(newVolume);
                    } catch (NumberFormatException ignored) {
                    }
                }
                event.deferEdit().queue();
                break;
                
            case "mute":
                if (!hasPermission(member, "music.volume")) {
                    replyNoPermission(event);
                    return;
                }
                if (player.getVolume() == 0) {
                    player.setVolume(50); // Unmute to default volume
                } else {
                    player.setVolume(0); // Mute
                }
                event.deferEdit().queue();
                break;
                
            default:
                event.reply(plugin.getPluginLanguageAdapter().getString(guild, "music.error.unknown_action"))
                     .setEphemeral(true)
                     .queue();
                break;
        }
    }
    
    private boolean hasPermission(Member member, String permission) {
        return plugin.getPluginPermissionManager().hasPermission(member.getUser(), permission);
    }
    
    private void replyNoPermission(ButtonInteractionEvent event) {
        event.reply(plugin.getPluginLanguageAdapter().getString(
            event.getGuild(), "music.error.no_permission"
        )).setEphemeral(true).queue();
    }
}