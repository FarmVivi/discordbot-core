package fr.farmvivi.discordbot.core.command;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SimpleCommandContext, focusing on deferReply functionality.
 */
@ExtendWith(MockitoExtension.class)
class SimpleCommandContextTest {

    @Mock
    private SlashCommandInteractionEvent slashCommandEvent;

    @Mock
    private Command command;

    @Mock
    private User user;

    @Mock
    private Guild guild;

    @Mock
    private MessageChannel channel;

    @Mock
    private LanguageManager languageManager;

    @Mock
    private ReplyCallbackAction replyCallbackAction;

    private SimpleCommandContext context;

    @BeforeEach
    void setUp() {
        context = new SimpleCommandContext(
            slashCommandEvent,
            command,
            user,
            guild,
            channel,
            Locale.US,
            new HashMap<>(),
            languageManager
        );
    }

    @Test
    void testDeferReplyWithEphemeralFlag() {
        // Arrange
        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.deferReply(anyBoolean())).thenReturn(replyCallbackAction);
        doNothing().when(replyCallbackAction).queue();

        // Act
        context.deferReply(true);

        // Assert
        verify(slashCommandEvent).deferReply(true); // Should call deferReply with ephemeral=true
        verify(replyCallbackAction).queue(); // Should queue the deferred reply
        assertTrue(context.isDeferred()); // Context should be marked as deferred
        assertTrue(context.isEphemeral()); // Context should be marked as ephemeral
    }

    @Test
    void testDeferReplyWithoutEphemeralFlag() {
        // Arrange
        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.deferReply(anyBoolean())).thenReturn(replyCallbackAction);
        doNothing().when(replyCallbackAction).queue();

        // Act
        context.deferReply(false);

        // Assert
        verify(slashCommandEvent).deferReply(false); // Should call deferReply with ephemeral=false
        verify(replyCallbackAction).queue(); // Should queue the deferred reply
        assertTrue(context.isDeferred()); // Context should be marked as deferred
        assertTrue(!context.isEphemeral()); // Context should not be marked as ephemeral
    }

    @Test
    void testDeferReplyDefaultsToNonEphemeral() {
        // Arrange
        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.deferReply(anyBoolean())).thenReturn(replyCallbackAction);
        doNothing().when(replyCallbackAction).queue();

        // Act
        context.deferReply(); // Default behavior should be non-ephemeral

        // Assert
        verify(slashCommandEvent).deferReply(false); // Should call deferReply with ephemeral=false
        verify(replyCallbackAction).queue(); // Should queue the deferred reply
        assertTrue(context.isDeferred()); // Context should be marked as deferred
        assertTrue(!context.isEphemeral()); // Context should not be marked as ephemeral
    }
}