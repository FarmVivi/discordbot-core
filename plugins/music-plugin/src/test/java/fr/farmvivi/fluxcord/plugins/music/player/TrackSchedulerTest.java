package fr.farmvivi.fluxcord.plugins.music.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import fr.farmvivi.fluxcord.plugins.music.MusicPlugin;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrackScheduler.
 */
public class TrackSchedulerTest {
    
    @Mock
    private MusicPlayer musicPlayer;
    
    @Mock
    private AudioPlayer audioPlayer;
    
    @Mock
    private MusicPlugin plugin;
    
    @Mock
    private Guild guild;
    
    @Mock
    private AudioTrack track1;
    
    @Mock
    private AudioTrack track2;
    
    @Mock
    private AudioTrackInfo trackInfo1;
    
    @Mock
    private AudioTrackInfo trackInfo2;
    
    private TrackScheduler scheduler;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mocks
        when(musicPlayer.getPlugin()).thenReturn(plugin);
        when(musicPlayer.getGuild()).thenReturn(guild);
        when(guild.getName()).thenReturn("Test Guild");
        
        when(track1.getInfo()).thenReturn(trackInfo1);
        when(track2.getInfo()).thenReturn(trackInfo2);
        when(trackInfo1.title).thenReturn("Track 1");
        when(trackInfo2.title).thenReturn("Track 2");
        
        scheduler = new TrackScheduler(musicPlayer, audioPlayer);
    }
    
    @Test
    void testQueueTrack() {
        // First track should play immediately
        when(audioPlayer.startTrack(track1, true)).thenReturn(true);
        boolean playing = scheduler.queue(track1);
        assertTrue(playing);
        verify(audioPlayer).startTrack(track1, true);
        
        // Second track should be queued
        when(audioPlayer.startTrack(track2, true)).thenReturn(false);
        playing = scheduler.queue(track2);
        assertFalse(playing);
        assertEquals(1, scheduler.getQueueSize());
    }
    
    @Test
    void testPlayNow() {
        // Setup current track
        when(audioPlayer.getPlayingTrack()).thenReturn(track1);
        when(track1.makeClone()).thenReturn(track1);
        
        // Play track2 immediately
        scheduler.playNow(track2);
        
        // Verify track2 starts playing
        verify(audioPlayer).startTrack(track2, false);
        
        // Verify track1 was added back to queue
        assertEquals(1, scheduler.getQueueSize());
    }
    
    @Test
    void testClearQueue() {
        // Add tracks to queue
        when(audioPlayer.startTrack(any(), anyBoolean())).thenReturn(false);
        scheduler.queue(track1);
        scheduler.queue(track2);
        assertEquals(2, scheduler.getQueueSize());
        
        // Clear queue
        scheduler.clear();
        assertEquals(0, scheduler.getQueueSize());
    }
    
    @Test
    void testLoopMode() {
        assertFalse(scheduler.isLoopMode());
        
        scheduler.setLoopMode(true);
        assertTrue(scheduler.isLoopMode());
        
        scheduler.setLoopMode(false);
        assertFalse(scheduler.isLoopMode());
    }
    
    @Test
    void testShuffleMode() {
        assertFalse(scheduler.isShuffleMode());
        
        scheduler.setShuffleMode(true);
        assertTrue(scheduler.isShuffleMode());
        
        scheduler.setShuffleMode(false);
        assertFalse(scheduler.isShuffleMode());
    }
    
    @Test
    void testRemoveTrack() {
        // Add tracks to queue
        when(audioPlayer.startTrack(any(), anyBoolean())).thenReturn(false);
        scheduler.queue(track1);
        scheduler.queue(track2);
        
        // Remove first track
        assertTrue(scheduler.removeTrack(0));
        assertEquals(1, scheduler.getQueueSize());
        
        // Try invalid index
        assertFalse(scheduler.removeTrack(5));
    }
}