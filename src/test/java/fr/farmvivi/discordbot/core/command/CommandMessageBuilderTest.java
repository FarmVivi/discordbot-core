package fr.farmvivi.discordbot.core.command;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommandMessageBuilder, focusing on ephemeral message handling.
 */
@ExtendWith(MockitoExtension.class)
class CommandMessageBuilderTest {

    @Mock
    private SlashCommandInteractionEvent slashCommandEvent;

    @Mock
    private LanguageManager languageManager;

    @Mock
    private ReplyCallbackAction replyCallbackAction;

    private CommandMessageBuilder messageBuilder;

    @BeforeEach
    void setUp() {
        messageBuilder = new CommandMessageBuilder(slashCommandEvent, languageManager, Locale.US);
    }

    @Test
    void testDeferReplyWithEphemeralFlag() {
        // Arrange
        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.deferReply(anyBoolean())).thenReturn(replyCallbackAction);
        doNothing().when(replyCallbackAction).queue();

        // Set up the message builder for deferral with ephemeral flag
        messageBuilder.setDiffer(true);
        messageBuilder.setEphemeral(true);

        // Act
        messageBuilder.replyNow();

        // Assert
        verify(slashCommandEvent).deferReply(true); // Should call deferReply with ephemeral=true
        verify(replyCallbackAction).queue(); // Should queue the deferred reply
    }

    @Test
    void testDeferReplyWithoutEphemeralFlag() {
        // Arrange
        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.deferReply(anyBoolean())).thenReturn(replyCallbackAction);
        doNothing().when(replyCallbackAction).queue();

        // Set up the message builder for deferral without ephemeral flag
        messageBuilder.setDiffer(true);
        messageBuilder.setEphemeral(false);

        // Act
        messageBuilder.replyNow();

        // Assert
        verify(slashCommandEvent).deferReply(false); // Should call deferReply with ephemeral=false
        verify(replyCallbackAction).queue(); // Should queue the deferred reply
    }

    @Test
    void testDirectReplyWithEphemeralFlag() {
        // Arrange
        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.reply(any(MessageCreateData.class))).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(anyBoolean())).thenReturn(replyCallbackAction);
        doNothing().when(replyCallbackAction).queue();

        // Set up the message builder for direct reply (not deferred) with ephemeral flag
        messageBuilder.setDiffer(false);
        messageBuilder.setEphemeral(true);
        messageBuilder.setContent("Test message");

        // Act
        messageBuilder.replyNow();

        // Assert
        verify(slashCommandEvent).reply(any(MessageCreateData.class)); // Should call reply
        verify(replyCallbackAction).setEphemeral(true); // Should set ephemeral flag
        verify(replyCallbackAction).queue(); // Should queue the reply
    }

    @Test
    void testEditDeferredReply() {
        // Arrange - simulate a deferred reply that's already acknowledged
        when(slashCommandEvent.isAcknowledged()).thenReturn(true);
        
        // Mock the interaction hook and edit action
        InteractionHook mockHook = mock(InteractionHook.class);
        WebhookMessageEditAction<Message> mockEditAction = mock(WebhookMessageEditAction.class);
        
        when(slashCommandEvent.getHook()).thenReturn(mockHook);
        when(mockHook.editOriginal(anyString())).thenReturn(mockEditAction);
        doNothing().when(mockEditAction).queue();

        // Set up the message builder for editing a deferred reply
        messageBuilder.setDiffer(true);
        messageBuilder.setEphemeral(true); // This should have no effect on editing
        messageBuilder.setContent("Updated message");

        // Act
        messageBuilder.replyNow();

        // Assert
        verify(mockHook).editOriginal("Updated message"); // Should edit the original message
        verify(mockEditAction).queue(); // Should queue the edit
        // Note: No ephemeral handling during edit - it was set during initial deferral
    }
}