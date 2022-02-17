package fr.farmvivi.discordbot.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Module {
    protected final Logger logger;
    protected final Modules module;

    protected boolean enabled = false;

    public Module(Modules module) {
        logger = LoggerFactory.getLogger(module.getName().replace(" ", "") + "Module");
        this.module = module;
    }

    public void enable() {

    }

    public void disable() {

    }

    public Logger getLogger() {
        return logger;
    }

    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
