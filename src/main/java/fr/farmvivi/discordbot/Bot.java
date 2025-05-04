package fr.farmvivi.discordbot;

import fr.farmvivi.discordbot.jda.JDAManager;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {
    public Bot() {
        setDefaultActivity();
    }

    public static Activity getDefaultActivity() {
        return Activity.playing("v" + version);
    }

    public static void setDefaultActivity() {
        JDAManager.getJDA().getPresence().setActivity(getDefaultActivity());
    }

    public static boolean isDefaultActivity() {
        Activity currentActivity = JDAManager.getJDA().getPresence().getActivity();
        if (currentActivity == null) {
            return false;
        }
        Activity defaultActivity = getDefaultActivity();
        return currentActivity.equals(defaultActivity);
    }
}
