package fr.farmvivi.discordbot.module.forms;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Form {
    private final UUID uuid;

    private final List<FormStep> steps = new ArrayList<>();
    private boolean cancelled = false;

    public Form() {
        this.uuid = UUID.randomUUID();
    }

    public abstract void start(IReplyCallback event);

    protected abstract void finish(IReplyCallback event);

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    public synchronized void addStep(FormStep step) {
        if (cancelled) {
            return;
        }

        step.setStep(steps.size());
        steps.add(step);
    }

    public void nextStep(IReplyCallback event) {
        if (cancelled) {
            finish(event);
            return;
        }

        for (FormStep step : steps) {
            if (!step.isQuestionSent()) {
                step.question(event);
                return;
            }
        }

        finish(event);
    }

    void onButtonInteraction(GenericInteractionCreateEvent event, int step) {
        if (cancelled) {
            return;
        }

        steps.get(step).response(event);
    }

    void onModalInteraction(GenericInteractionCreateEvent event, int step) {
        if (cancelled) {
            return;
        }

        steps.get(step).response(event);
    }

    void onStringSelectInteraction(GenericInteractionCreateEvent event, int step) {
        if (cancelled) {
            return;
        }

        steps.get(step).response(event);
    }

    void onEntitySelectInteraction(GenericComponentInteractionCreateEvent event, int step) {
        if (cancelled) {
            return;
        }

        steps.get(step).response(event);
    }

    String getDiscordID(int step) {
        return FormsModule.getDiscordID(uuid, step);
    }

    String getDiscordID(int step, String customID) {
        return FormsModule.getDiscordID(uuid, step, customID);
    }

    protected String getCustomID(String discordID) {
        return FormsModule.getFormCustomID(discordID);
    }

    public UUID getUUID() {
        return uuid;
    }
}
