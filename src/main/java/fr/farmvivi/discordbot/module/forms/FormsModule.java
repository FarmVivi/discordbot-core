package fr.farmvivi.discordbot.module.forms;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.jda.JDAManager;
import fr.farmvivi.discordbot.module.Module;
import fr.farmvivi.discordbot.module.Modules;

import java.util.*;

public class FormsModule extends Module {
    public static final String FORMS_ID_PREFIX = "discordbot-form";

    private final Bot bot;
    private final FormsEventHandler formsEventHandler;

    private final Map<Modules, List<Form>> forms = new HashMap<>();

    public FormsModule(Bot bot) {
        super(Modules.FORMS);

        this.bot = bot;
        this.formsEventHandler = new FormsEventHandler(this);
    }

    @Override
    public void onPostEnable() {
        super.onPostEnable();

        logger.info("Registering event listener...");

        JDAManager.getJDA().addEventListener(formsEventHandler);
    }

    @Override
    public void onPreDisable() {
        super.onPreDisable();

        logger.info("Unregistering event listener...");

        JDAManager.getJDA().removeEventListener(formsEventHandler);
    }

    public void registerForm(Modules module, Form form) {
        logger.debug("Registering form " + form.getUUID() + "...");

        if (!forms.containsKey(module))
            forms.put(module, new ArrayList<>());

        List<Form> moduleForms = forms.get(module);

        moduleForms.add(form);
    }

    public void unregisterForm(Modules module, Form form) {
        logger.debug("Unregistering form " + form.getUUID() + "...");

        List<Form> moduleForms = forms.get(module);

        moduleForms.remove(form);
    }

    public void unregisterForms(Modules module) {
        forms.remove(module);
    }

    public List<Form> getForms(Modules module) {
        return forms.get(module);
    }

    public List<Form> getForms() {
        List<Form> forms = new ArrayList<>();

        for (List<Form> moduleForms : this.forms.values()) {
            forms.addAll(moduleForms);
        }

        return forms;
    }

    public static String getDiscordID(UUID uuid, int step) {
        return FormsModule.FORMS_ID_PREFIX + "-" + uuid + "-" + step;
    }

    public static String getDiscordID(UUID uuid, int step, String other) {
        return FormsModule.FORMS_ID_PREFIX + "-" + uuid + "-" + step + "-" + other;
    }

    public static UUID getFormUUID(String discordID) {
        // discordID = discordbot-form-<UUID>-<step>-<customID>
        int index = FormsModule.FORMS_ID_PREFIX.length() + 1;
        return UUID.fromString(discordID.substring(index, index + 36));
    }

    public static int getFormStep(String discordID) {
        // discordID = discordbot-form-<UUID>-<step>-<customID>
        int startIndex = FormsModule.FORMS_ID_PREFIX.length() + 1 + 36 + 1;
        int endIndex = discordID.indexOf('-', startIndex);
        if (endIndex == -1) {
            endIndex = discordID.length();
        }
        return Integer.parseInt(discordID.substring(startIndex, endIndex));
    }

    public static String getFormCustomID(String discordID) {
        // discordID = discordbot-form-<UUID>-<step>-<customID>
        int startIndex = FormsModule.FORMS_ID_PREFIX.length() + 1 + 36 + 1;
        int endIndex = discordID.indexOf('-', startIndex);
        if (endIndex == -1) {
            return null;
        }
        return discordID.substring(endIndex + 1);
    }
}
