package fr.farmvivi.discordbot.module.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

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

                // Delete old message
                if (message != null) {
                    message.delete().queue();
                    message = null;
                }

                if (musicPlayer.getGuild().getAudioManager().isConnected()) {
                    // Create embed message
                    MessageCreateBuilder builder = new MessageCreateBuilder();
                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    AudioTrack track = musicPlayer.getAudioPlayer().getPlayingTrack();

                    // Check if track is null = no music playing
                    if (track == null) {
                        embedBuilder.setTitle("Aucune musique en cours de lecture");
                    }
                    // Check if track is paused
                    else if (musicPlayer.getAudioPlayer().isPaused()) {
                        embedBuilder.setTitle("Musique en pause");
                    }
                    // Track is playing
                    else {
                        embedBuilder.setTitle("Musique en cours de lecture");
                    }

                    if (track != null) {
                        // Add track info title (with link)
                        embedBuilder.addField("Titre", String.format("[%s](%s)", track.getInfo().title, track.getInfo().uri), false);
                        if (!musicPlayer.getAudioPlayer().isPaused() && (musicPlayer.getListener().getTrackSize() == 0 || musicPlayer.isShuffleMode())) {
                            // Add track ending time
                            long endingTime = System.currentTimeMillis() + (track.getDuration() - track.getPosition());
                            embedBuilder.addField("Fin", String.format("<t:%d:R>", endingTime / 1000), false);
                        }
                        // Add queue size
                        if (musicPlayer.getListener().getTrackSize() > 0) {
                            StringBuilder topQueue = new StringBuilder();
                            if (musicPlayer.isShuffleMode()) {
                                topQueue.append("Mode aléatoire");
                            } else {
                                int i = 0;
                                long endingTime = System.currentTimeMillis() + (track.getDuration() - track.getPosition());
                                for (AudioTrack queueTrack : musicPlayer.getListener().getTracks()) {
                                    if (musicPlayer.getAudioPlayer().isPaused()) {
                                        topQueue.append(String.format("%s. [%s](%s)%n", i + 1, queueTrack.getInfo().title, queueTrack.getInfo().uri));
                                    } else {
                                        topQueue.append(String.format("%s. [%s](%s) - <t:%d:R>%n", i + 1, queueTrack.getInfo().title, queueTrack.getInfo().uri, endingTime / 1000));
                                        endingTime += queueTrack.getDuration();
                                    }
                                    i++;
                                    // Limit to 5 tracks
                                    if (i >= 5) {
                                        break;
                                    }
                                }
                            }
                            embedBuilder.addField("File d'attente (" + musicPlayer.getListener().getTrackSize() + ")", topQueue.toString(), false);
                        }
                        // Add volume
                        embedBuilder.addField("Volume", String.format("%d%%", musicPlayer.getAudioPlayer().getVolume()), false);
                    }

                    builder.setEmbeds(embedBuilder.build());

                    // Add quick action buttons
                    List<ItemComponent> buttonsRow1 = new ArrayList<>();
                    List<ItemComponent> buttonsRow2 = new ArrayList<>();

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
                    if (musicPlayer.getListener().getTrackSize() > 0) {
                        Button nextButton = Button.primary(getButtonID("skip"), "⏭️");
                        buttonsRow1.add(nextButton);
                    }

                    // Stop
                    if (track != null) {
                        Button stopButton = Button.secondary(getButtonID("stop"), "⏹️");
                        buttonsRow1.add(stopButton);
                    }

                    // Clear queue
                    if (musicPlayer.getListener().getTrackSize() > 0) {
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

                    // Add buttons to message
                    if (!buttonsRow1.isEmpty()) {
                        builder.addActionRow(buttonsRow1);
                    }
                    if (!buttonsRow2.isEmpty()) {
                        builder.addActionRow(buttonsRow2);
                    }

                    // Send message
                    if (messageChannel != null) {
                        // Send message and release semaphore
                        // TODO Edit message instead of sending new one if no new message was sent in the channel
                        messageChannel.sendMessage(builder.build()).queue(m -> {
                            message = m;
                            semaphore.release();
                        }, e -> semaphore.release());
                    } else {
                        semaphore.release();
                    }
                } else {
                    semaphore.release();
                }
            }
        };

        // Schedule timer task
        debounceTimer.schedule(debounceTask, 1000);
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
