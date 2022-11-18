package fr.farmvivi.discordbot.module.forms;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public abstract class FormStep {
    private final Form form;

    private int step;
    private boolean questionSent = false;
    private boolean responseReceived = false;

    public FormStep(Form form) {
        this.form = form;
    }

    synchronized void question(IReplyCallback event) {
        if (form.isCancelled()) {
            event.reply("> Erreur : Le formulaire a été annulé.").queue();
            return;
        }

        if (questionSent) {
            event.reply("> Erreur : La question a déjà été envoyée.").queue();
            return;
        }

        questionSent = true;

        handleQuestion(event);

        if (!questionSent) {
            clean();

            form.nextStep(event);
        }
    }

    protected abstract void handleQuestion(IReplyCallback event);

    protected void questionError(IReplyCallback event, String errorMessage) {
        event.reply("> :x: " + errorMessage).queue();

        questionSent = false;
    }

    synchronized void response(GenericInteractionCreateEvent event) {
        if (form.isCancelled()) {
            if (event instanceof IReplyCallback replyCallback) {
                replyCallback.reply("> Erreur : Le formulaire a été annulé.").queue();
            }
            return;
        }

        if (!questionSent) {
            if (event instanceof IReplyCallback replyCallback) {
                replyCallback.reply("> Erreur : La question n'a pas encore été envoyée.").queue();
            }
            return;
        }

        if (responseReceived) {
            if (event instanceof IReplyCallback replyCallback) {
                replyCallback.reply("> Erreur : La réponse a déjà été reçue.").queue();
            }
            return;
        }

        responseReceived = true;

        handleResponse(event);

        clean();

        if (event instanceof IReplyCallback replyCallback) {
            form.nextStep(replyCallback);
        } else {
            form.cancel();
        }
    }

    protected abstract void handleResponse(GenericInteractionCreateEvent event);

    protected void responseError(GenericInteractionCreateEvent event, String errorMessage) {
        if (event instanceof IReplyCallback replyCallback) {
            replyCallback.reply("> :x: " + errorMessage).queue();
        }

        responseReceived = false;
    }

    protected void skipStep(IReplyCallback event) {
        questionSent = true;
        responseReceived = true;

        clean();

        form.nextStep(event);
    }

    protected abstract void clean();

    public Form getForm() {
        return form;
    }

    public int getStep() {
        return step;
    }

    void setStep(int step) {
        this.step = step;
    }

    public boolean isQuestionSent() {
        return questionSent;
    }

    public boolean isResponseReceived() {
        return responseReceived;
    }

    public String getDiscordID() {
        return form.getDiscordID(step);
    }

    public String getDiscordID(String customID) {
        return form.getDiscordID(step, customID);
    }

    public String getCustomID(String discordID) {
        return form.getCustomID(discordID);
    }
}
