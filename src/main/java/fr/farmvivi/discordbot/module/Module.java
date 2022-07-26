package fr.farmvivi.discordbot.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Module {
    protected final Logger logger;
    protected final Modules module;

    protected ModulePhase status = ModulePhase.LOADED;

    public Module(Modules module) {
        this.module = module;
        this.logger = LoggerFactory.getLogger(module.getName().replace(" ", "") + "Module");
    }

    public void onPreEnable() {

    }

    public void onPreDisable() {

    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void onPostEnable() {

    }

    public void onPostDisable() {

    }

    public Logger getLogger() {
        return logger;
    }

    public ModulePhase getStatus() {
        return status;
    }

    public void setStatus(ModulePhase status) {
        this.status = status;
    }
}
