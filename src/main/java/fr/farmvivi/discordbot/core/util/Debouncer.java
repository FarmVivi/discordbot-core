package fr.farmvivi.discordbot.core.util;

import java.util.Timer;
import java.util.TimerTask;

public class Debouncer {
    private final long delayMillis;
    private final Runnable action;
    private Timer timer;

    /**
     * Crée un debouncer avec un délai spécifié et une action à exécuter.
     *
     * @param delayMillis Le délai en millisecondes
     * @param action      L'action à exécuter
     */
    public Debouncer(long delayMillis, Runnable action) {
        this.delayMillis = delayMillis;
        this.action = action;
    }

    /**
     * Déclenche l'action après le délai spécifié.
     * Si cette méthode est appelée plusieurs fois rapidement, l'action ne sera exécutée qu'une seule fois après le délai spécifié.
     */
    public synchronized void debounce() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        }, delayMillis);
    }
}
