package fr.farmvivi.discordbot.module.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.farmvivi.discordbot.DiscordColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class MusicPlayerMessage {
    private final Semaphore semaphore = new Semaphore(1);
    private final MusicPlayer musicPlayer;
    private MessageChannelUnion messageChannel;
    private Message message;
    private Timer debounceTimer;

    public MusicPlayerMessage(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    public synchronized void refreshMessage() {
        // Cancel old timer
        if (debounceTimer != null) {
            debounceTimer.cancel();
        }

        // Create new timer
        debounceTimer = new Timer();

        // Init timer task
        TimerTask debounceTask = new TimerTask() {
            @Override
            public void run() {
                // Acquire semaphore
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Logger logger = musicPlayer.getMusicModule().getLogger();
                Guild guild = musicPlayer.getGuild();

                // If bot is disconnected
                if (!musicPlayer.getGuild().getAudioManager().isConnected()) {
                    // Delete old message
                    if (message != null) {
                        // Log : [<Guild name> (Guild id)] Deleting player message...
                        logger.info(String.format("[%s (%s)] Deleting player message...", guild.getName(), guild.getId()));

                        message.delete().queue();
                        message = null;
                    }

                    // Release semaphore
                    semaphore.release();
                }
                // If bot is connected
                else {
                    // Log : [<Guild name> (Guild id)] Refreshing player message...
                    logger.debug(String.format("[%s (%s)] Refreshing player message...", guild.getName(), guild.getId()));

                    // Create embed message
                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    AudioTrack track = musicPlayer.getAudioPlayer().getPlayingTrack();

                    // Check if track is null = no music playing
                    if (track == null) {
                        embedBuilder.setTitle("Aucune musique en cours de lecture");
                        embedBuilder.setColor(DiscordColor.RED.getColor());
                    }
                    // Check if track is paused
                    else if (musicPlayer.getAudioPlayer().isPaused()) {
                        embedBuilder.setTitle("Musique en pause");
                        embedBuilder.setColor(DiscordColor.ORANGE.getColor());
                    }
                    // Track is playing
                    else {
                        embedBuilder.setTitle("Musique en cours de lecture");
                        embedBuilder.setColor(DiscordColor.GREEN.getColor());
                    }

                    if (track != null) {
                        // Add track info title (with link)
                        embedBuilder.addField("Titre", String.format("[%s](%s)", track.getInfo().title, track.getInfo().uri), false);
                        if (!musicPlayer.getAudioPlayer().isPaused() && (musicPlayer.getQueueSize() == 0 || musicPlayer.isShuffleMode())) {
                            // Add track ending time
                            StringBuilder endingTime = new StringBuilder();
                            if (track.getDuration() != Long.MAX_VALUE) {
                                long endingTimeMs = System.currentTimeMillis() + (track.getDuration() - track.getPosition());
                                endingTime.append(String.format("<t:%d:R>", endingTimeMs / 1000));
                            } else {
                                endingTime.append("∞");
                            }
                            embedBuilder.addField("Fin", endingTime.toString(), false);
                        }
                        // Add queue size
                        if (musicPlayer.getQueueSize() > 0) {
                            StringBuilder topQueue = new StringBuilder();
                            if (musicPlayer.isShuffleMode()) {
                                topQueue.append("Mode aléatoire");
                            } else {
                                int i = 0;
                                long endingTimeMs = -1;
                                if (track.getDuration() != Long.MAX_VALUE) {
                                    endingTimeMs = System.currentTimeMillis() + (track.getDuration() - track.getPosition());
                                }
                                for (AudioTrack queueTrack : musicPlayer.getQueue()) {
                                    if (musicPlayer.getAudioPlayer().isPaused() || endingTimeMs == -1) {
                                        topQueue.append(String.format("%s. [%s](%s)%n", i + 1, queueTrack.getInfo().title, queueTrack.getInfo().uri));
                                    } else {
                                        topQueue.append(String.format("%s. [%s](%s) - <t:%d:R>%n", i + 1, queueTrack.getInfo().title, queueTrack.getInfo().uri, endingTimeMs / 1000));
                                        if (queueTrack.getDuration() != Long.MAX_VALUE) {
                                            endingTimeMs += queueTrack.getDuration();
                                        } else {
                                            endingTimeMs = -1;
                                        }
                                    }
                                    i++;
                                    // Limit to 5 tracks
                                    if (i >= 5) {
                                        break;
                                    }
                                }
                            }
                            embedBuilder.addField("File d'attente (" + musicPlayer.getQueueSize() + ")", topQueue.toString(), false);
                        }
                    }

                    // Add quick action buttons
                    List<ItemComponent> buttonsRow1 = new ArrayList<>();
                    List<ItemComponent> buttonsRow2 = new ArrayList<>();
                    List<ItemComponent> buttonsRow3 = new ArrayList<>();

                    // Add to queue
                    Button addToQueueButton = Button.primary(getButtonID("add"), "🆕");
                    buttonsRow1.add(addToQueueButton);

                    // Play / Pause
                    if (track != null) {
                        if (musicPlayer.getAudioPlayer().isPaused()) {
                            Button pauseButton = Button.primary(getButtonID("pause"), "▶️");
                            buttonsRow1.add(pauseButton);
                        } else {
                            Button playButton = Button.primary(getButtonID("pause"), "⏸️");
                            buttonsRow1.add(playButton);
                        }
                    }

                    // Skip track
                    if (musicPlayer.getQueueSize() > 0) {
                        Button nextButton = Button.primary(getButtonID("skip"), "⏭️");
                        buttonsRow1.add(nextButton);
                    }

                    // Stop
                    if (track != null) {
                        Button stopButton = Button.secondary(getButtonID("stop"), "⏹️");
                        buttonsRow1.add(stopButton);
                    }

                    // Clear queue
                    if (musicPlayer.getQueueSize() > 0) {
                        Button clearQueueButton = Button.secondary(getButtonID("clearqueue"), "🗑️");
                        buttonsRow1.add(clearQueueButton);
                    }

                    // Loop
                    if (musicPlayer.isLoopMode()) {
                        Button loopButton = Button.success(getButtonID("loop"), "🔂");
                        buttonsRow2.add(loopButton);
                    } else {
                        Button loopButton = Button.danger(getButtonID("loop"), "🔂");
                        buttonsRow2.add(loopButton);
                    }

                    // Loop queue
                    if (musicPlayer.isLoopQueueMode()) {
                        Button loopQueueButton = Button.success(getButtonID("loopqueue"), "🔁");
                        buttonsRow2.add(loopQueueButton);
                    } else {
                        Button loopQueueButton = Button.danger(getButtonID("loopqueue"), "🔁");
                        buttonsRow2.add(loopQueueButton);
                    }

                    // Shuffle
                    if (musicPlayer.isShuffleMode()) {
                        Button shuffleButton = Button.success(getButtonID("shuffle"), "🔀");
                        buttonsRow2.add(shuffleButton);
                    } else {
                        Button shuffleButton = Button.danger(getButtonID("shuffle"), "🔀");
                        buttonsRow2.add(shuffleButton);
                    }

                    // Volume
                    // -10%
                    Button volumeDown10Button = Button.secondary(getButtonID("volumedown10"), "-10%");
                    buttonsRow3.add(volumeDown10Button);
                    // -5%
                    Button volumeDown5Button = Button.secondary(getButtonID("volumedown5"), "-5%");
                    buttonsRow3.add(volumeDown5Button);
                    // Mute/Unmute (view volume)
                    if (musicPlayer.getVolume() == 0) {
                        Button volumeMuteButton = Button.danger(getButtonID("volumemute"), "🔇");
                        buttonsRow3.add(volumeMuteButton);
                    } else {
                        Button volumeMuteButton = Button.success(getButtonID("volumemute"), "🔊 " + String.format("%d%%", musicPlayer.getAudioPlayer().getVolume()));
                        buttonsRow3.add(volumeMuteButton);
                    }
                    // +5%
                    Button volumeUp5Button = Button.secondary(getButtonID("volumeup5"), "+5%");
                    buttonsRow3.add(volumeUp5Button);
                    // +10%
                    Button volumeUp10Button = Button.secondary(getButtonID("volumeup10"), "+10%");
                    buttonsRow3.add(volumeUp10Button);

                    // END, release semaphore
                    if (messageChannel == null) {
                        semaphore.release();
                    }
                    // Send message and release semaphore
                    else {
                        /*
                         * ALGORITHM
                         * 1. Get the last message of the channel
                         * 2. If the last message is the message of the player, edit it
                         * 3. Else, delete old player message and send new one
                         */
                        messageChannel.getHistory().retrievePast(1).queue(messages -> {
                            if (!messages.isEmpty()) {
                                Message lastMessage = messages.get(0);
                                if (message != null && lastMessage != null && lastMessage.getIdLong() == message.getIdLong()) {
                                    // Edit message
                                    MessageEditBuilder builder = new MessageEditBuilder();
                                    builder.setEmbeds(embedBuilder.build());
                                    builder.setComponents(ActionRow.of(buttonsRow1), ActionRow.of(buttonsRow2), ActionRow.of(buttonsRow3));
                                    lastMessage.editMessage(builder.build()).queue(m -> {
                                        message = m;
                                        semaphore.release();
                                    }, e -> semaphore.release());
                                    return;
                                }
                            }
                            // Delete old message, send new message and release semaphore
                            MessageCreateBuilder builder = new MessageCreateBuilder();
                            builder.setEmbeds(embedBuilder.build());
                            builder.setComponents(ActionRow.of(buttonsRow1), ActionRow.of(buttonsRow2), ActionRow.of(buttonsRow3));
                            messageChannel.sendMessage(builder.build()).queue(m -> {
                                // Delete old message
                                if (message != null) {
                                    message.delete().queue();
                                }
                                message = m;
                                semaphore.release();
                            }, e -> semaphore.release());
                        });
                    }
                }
            }
        };

        // Schedule timer task
        debounceTimer.schedule(debounceTask, 500);
    }

    private String getButtonID(String action) {
        return MusicModule.getDiscordID(musicPlayer.getGuild(), action);
    }

    public void setMessageChannel(MessageChannelUnion messageChannel) {
        this.messageChannel = messageChannel;
    }

    public Message getMessage() {
        return message;
    }
}
