package fr.farmvivi.discordbot.module.forms;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.UUID;

public class FormsEventHandler extends ListenerAdapter {
    private final FormsModule module;

    public FormsEventHandler(FormsModule module) {
        this.module = module;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith(FormsModule.FORMS_ID_PREFIX)) {
            return;
        }

        UUID formUUID = FormsModule.getFormUUID(id);

        for (Form form : module.getForms()) {
            if (form.getUUID().equals(formUUID)) {
                int formStep = FormsModule.getFormStep(id);

                form.onButtonInteraction(event, formStep);
                return;
            }
        }

        event.reply("> :x: Le formulaire n'existe plus.").setEphemeral(true).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String id = event.getModalId();
        if (!id.startsWith(FormsModule.FORMS_ID_PREFIX)) {
            return;
        }

        UUID formUUID = FormsModule.getFormUUID(id);

        for (Form form : module.getForms()) {
            if (form.getUUID().equals(formUUID)) {
                int formStep = FormsModule.getFormStep(id);

                form.onModalInteraction(event, formStep);
                return;
            }
        }

        event.reply("> :x: Le formulaire n'existe plus.").setEphemeral(true).queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith(FormsModule.FORMS_ID_PREFIX)) {
            return;
        }

        UUID formUUID = FormsModule.getFormUUID(id);

        for (Form form : module.getForms()) {
            if (form.getUUID().equals(formUUID)) {
                int formStep = FormsModule.getFormStep(id);

                form.onStringSelectInteraction(event, formStep);
                return;
            }
        }

        event.reply("> :x: Le formulaire n'existe plus.").setEphemeral(true).queue();
    }

    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith(FormsModule.FORMS_ID_PREFIX)) {
            return;
        }

        UUID formUUID = FormsModule.getFormUUID(id);

        for (Form form : module.getForms()) {
            if (form.getUUID().equals(formUUID)) {
                int formStep = FormsModule.getFormStep(id);

                form.onEntitySelectInteraction(event, formStep);
                return;
            }
        }

        event.reply("> :x: Le formulaire n'existe plus.").setEphemeral(true).queue();
    }
}
