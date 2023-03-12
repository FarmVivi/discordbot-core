package fr.farmvivi.discordbot.module.music;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class MusicEventHandler extends ListenerAdapter {
    private final MusicModule musicModule;

    public MusicEventHandler(MusicModule musicModule) {
        this.musicModule = musicModule;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        super.onGuildVoiceUpdate(event);

        // If the bot
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            Logger logger = musicModule.getLogger();
            Guild guild = event.getGuild();

            AudioChannelUnion joinedChannel = event.getChannelJoined();
            AudioChannelUnion leftChannel = event.getChannelLeft();

            // If the bot joined a channel
            if (joinedChannel != null && leftChannel == null) {
                // Log : [<Guild name> (Guild id)] Bot joined channel "Channel name" (Channel id)
                logger.info(String.format("[%s (%s)] Bot joined channel \"%s\" (%s)", guild.getName(), guild.getId(), joinedChannel.getName(), joinedChannel.getId()));
            }
            // If the bot left a channel
            else if (leftChannel != null && joinedChannel == null) {
                // Log : [<Guild name> (Guild id)] Bot left channel "Channel name" (Channel id)
                logger.info(String.format("[%s (%s)] Bot left channel \"%s\" (%s)", guild.getName(), guild.getId(), leftChannel.getName(), leftChannel.getId()));

                MusicPlayer musicPlayer = musicModule.getPlayer(event.getGuild());
                musicPlayer.getAudioPlayer().setPaused(false);
                musicPlayer.clearQueue();
                musicPlayer.skipTrack();
                musicPlayer.resetToDefaultSettings();
                Bot.setDefaultActivity();
            }
            // If the bot moved to another channel
            else if (leftChannel != null && joinedChannel != null) {
                // Log : [<Guild name> (Guild id)] Bot moved from channel "Channel name" (Channel id) to channel "Channel name" (Channel id)
                logger.info(String.format("[%s (%s)] Bot moved from channel \"%s\" (%s) to channel \"%s\" (%s)", guild.getName(), guild.getId(), leftChannel.getName(), leftChannel.getId(), joinedChannel.getName(), joinedChannel.getId()));
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // Check if button is part of the music module
        String id = event.getComponentId();
        if (!id.startsWith(MusicModule.PLAYER_ID_PREFIX)) {
            return;
        }

        String guildId = MusicModule.getGuildID(id);
        if (!guildId.equals(event.getGuild().getId())) {
            // Send error message
            event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
            return;
        }

        // Check permission (if user is in the same voice channel as the bot)
        // Check if member exists
        Member member = event.getMember();
        if (member == null) {
            event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
            return;
        }

        // Check if bot is connected to a voice channel
        GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (botVoiceState == null || botVoiceState.getChannel() == null) {
            event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
            return;
        }

        // Check if member is connected to a voice channel
        GuildVoiceState memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || memberVoiceState.getChannel() == null) {
            event.reply("Veuillez rejoindre le salon vocal du bot pour effectuer cette action.").setEphemeral(true).queue();
            return;
        }

        // Check if member and bot are in the same voice channel
        if (!memberVoiceState.getChannel().equals(botVoiceState.getChannel())) {
            event.reply("Veuillez rejoindre le salon vocal du bot pour effectuer cette action.").setEphemeral(true).queue();
            return;
        }

        MusicPlayer musicPlayer = musicModule.getPlayer(event.getGuild());

        String action = MusicModule.getAction(id);
        switch (action) {
            case "add" -> {
                // Create modal
                Modal.Builder addToQueueModalBuilder = Modal.create(MusicModule.getDiscordID(musicPlayer.getGuild(), "add"), "Ajouter une musique");

                // Music provider (future, when supported by discord)
                /*SelectMenu musicProvidersList = StringSelectMenu.create(MusicModule.getDiscordID(musicPlayer.getGuild(), "musicproviders"))
                        .setPlaceholder("Choisissez un service")
                        .addOption("Par défaut", "default")
                        .addOption("URL", "url")
                        .addOption("Recherche YouTube", "youtube")
                        .addOption("Recherche YouTube Music", "youtubemusic")
                        .addOption("Recherche Spotify", "spotify")
                        .addOption("Recherche Deezer", "deezer")
                        .addOption("Recherche SoundCloud", "soundcloud")
                        .addOption("Recherche Apple Music", "applemusic")
                        .setRequiredRange(1, 1)
                        .setDefaultValues("default")
                        .build();
                addToQueueModalBuilder.addActionRow(musicProvidersList);*/

                // Music URL
                TextInput musicURLInput = TextInput.create(MusicModule.getDiscordID(musicPlayer.getGuild(), "musicurl"), "Musique à ajouter", TextInputStyle.SHORT)
                        .setRequired(true)
                        .build();
                addToQueueModalBuilder.addActionRow(musicURLInput);

                // Reply to interaction
                event.replyModal(addToQueueModalBuilder.build()).queue();
            }
            case "pause" -> musicPlayer.getAudioPlayer().setPaused(!musicPlayer.getAudioPlayer().isPaused());
            case "skip" -> {
                if (musicPlayer.isLoopQueueMode()) {
                    musicPlayer.nextTrack();
                } else {
                    musicPlayer.skipTrack();
                }
            }
            case "stop" -> {
                musicPlayer.getAudioPlayer().setPaused(false);
                musicPlayer.clearQueue();
                musicPlayer.skipTrack();
            }
            case "clearqueue" -> musicPlayer.clearQueue();
            case "loop" -> musicPlayer.setLoopMode(!musicPlayer.isLoopMode());
            case "loopqueue" -> musicPlayer.setLoopQueueMode(!musicPlayer.isLoopQueueMode());
            case "shuffle" -> musicPlayer.setShuffleMode(!musicPlayer.isShuffleMode());
            case "volumedown10" -> {
                int volume = musicPlayer.getVolume() - 10;
                if (volume < 0) {
                    volume = 0;
                }
                musicPlayer.setVolume(volume);
            }
            case "volumedown5" -> {
                int volume = musicPlayer.getVolume() - 5;
                if (volume < 0) {
                    volume = 0;
                }
                musicPlayer.setVolume(volume);
            }
            case "volumeup5" -> {
                int volume = musicPlayer.getVolume() + 5;
                if (volume > 100) {
                    volume = 100;
                }
                musicPlayer.setVolume(volume);
            }
            case "volumeup10" -> {
                int volume = musicPlayer.getVolume() + 10;
                if (volume > 100) {
                    volume = 100;
                }
                musicPlayer.setVolume(volume);
            }
            case "volumemute" -> {
                if (musicPlayer.getVolume() == 0) {
                    musicPlayer.unmute();
                } else {
                    musicPlayer.mute();
                }
            }
            default -> event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
        }

        // Delete interaction
        if (!event.isAcknowledged()) {
            event.deferEdit().queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        // Check if modal is from music module
        String id = event.getModalId();
        if (!id.startsWith(MusicModule.PLAYER_ID_PREFIX)) {
            return;
        }

        String guildId = MusicModule.getGuildID(id);
        if (!guildId.equals(event.getGuild().getId())) {
            // Send error message
            event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
            return;
        }

        // Check permission (if user is in the same voice channel as the bot)
        // Check if member exists
        Member member = event.getMember();
        if (member == null) {
            event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
            return;
        }

        // Check if bot is connected to a voice channel
        GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (botVoiceState == null || botVoiceState.getChannel() == null) {
            event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
            return;
        }

        // Check if member is connected to a voice channel
        GuildVoiceState memberVoiceState = member.getVoiceState();
        if (memberVoiceState == null || memberVoiceState.getChannel() == null) {
            event.reply("Veuillez rejoindre le salon vocal du bot pour effectuer cette action.").setEphemeral(true).queue();
            return;
        }

        // Check if member and bot are in the same voice channel
        if (!memberVoiceState.getChannel().equals(botVoiceState.getChannel())) {
            event.reply("Veuillez rejoindre le salon vocal du bot pour effectuer cette action.").setEphemeral(true).queue();
            return;
        }

        MusicPlayer musicPlayer = musicModule.getPlayer(event.getGuild());

        String action = MusicModule.getAction(id);
        switch (action) {
            case "add" -> {
                // Get music URL
                String musicURL = event.getValue(MusicModule.getDiscordID(musicPlayer.getGuild(), "musicurl")).getAsString();

                CommandMessageBuilder reply = new CommandMessageBuilder(event);

                // Add music to queue
                musicModule.loadTrack(musicPlayer.getGuild(), musicURL, null, reply, false);

                if (!event.isAcknowledged()) {
                    if (reply.isDiffer()) {
                        event.deferReply(reply.isEphemeral()).queue();
                    } else if (reply.isEmpty()) {
                        event.reply("OK").flatMap(InteractionHook::deleteOriginal).queue();
                    } else {
                        event.reply(reply.build()).setEphemeral(reply.isEphemeral()).queue();
                    }
                }
            }
            default -> event.reply("Une erreur est survenue, veuillez réessayer.").setEphemeral(true).queue();
        }

        // Delete interaction
        if (!event.isAcknowledged()) {
            event.deferEdit().queue();
        }
    }
}
