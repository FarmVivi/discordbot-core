package fr.farmvivi.discordbot.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Module {
    protected final Logger logger;
    protected final Modules module;

    protected boolean enabled = false;

    public Module(Modules module) {
        this.module = module;
        this.logger = LoggerFactory.getLogger(module.getName().replace(" ", "") + "Module");
    }

    public abstract void enable();

    public abstract void disable();

    public Logger getLogger() {
        return logger;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
