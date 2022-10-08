package fr.farmvivi.discordbot.module;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.commands.CommandsModule;
import fr.farmvivi.discordbot.module.general.GeneralModule;
import fr.farmvivi.discordbot.module.goulag.GoulagModule;
import fr.farmvivi.discordbot.module.music.MusicModule;
import fr.farmvivi.discordbot.module.test.TestModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ModulesManager {
    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final Bot bot;

    private final Map<Modules, Module> modules = new HashMap<>();

    public ModulesManager(Bot bot) {
        this.bot = bot;
    }

    public void loadModules() {
        logger.info("Loading modules...");

        for (String moduleName : bot.getConfiguration().features) {
            if (moduleName.length() == 0)
                continue;

            try {
                Modules moduleType = Modules.valueOf(moduleName);
                loadModule(moduleType);
            } catch (IllegalArgumentException e) {
                logger.warn("Module " + moduleName + " not found");
            }
        }

        logger.info("Pre enabling modules...");
        changeStatuses(ModulePhase.PRE_ENABLE);

        logger.info("Enabling modules...");
        changeStatuses(ModulePhase.ENABLE);

        logger.info("Post enabling modules...");
        changeStatuses(ModulePhase.POST_ENABLE);

        logger.info("Enabled modules...");
        changeStatuses(ModulePhase.ENABLED);

        logger.info("Modules enabled");
    }

    private void loadModule(Modules moduleType) {
        if (modules.containsKey(moduleType)) {
            return;
        }

        for (Modules requiredModule : moduleType.getRequiredModules()) {
            loadModule(requiredModule);
        }

        logger.info("Loading " + moduleType.getName() + " module...");

        Module module;
        switch (moduleType) {
            case COMMANDS -> module = new CommandsModule(bot);
            case GENERAL -> module = new GeneralModule(bot);
            case GOULAG -> module = new GoulagModule(bot);
            case MUSIC -> module = new MusicModule(bot);
            case TEST -> module = new TestModule(bot);
            default -> {
                logger.error("Module " + moduleType + " has no implementation !");
                System.exit(4);
                return;
            }
        }
        modules.put(moduleType, module);
    }

    private void changeStatuses(ModulePhase newStatus) {
        for (Modules moduleType : modules.keySet()) {
            changeStatus(moduleType, newStatus);
        }
    }

    private void changeStatus(Modules moduleType, ModulePhase newStatus) {
        Module module = modules.get(moduleType);

        if (module.getStatus().equals(newStatus)) {
            return;
        }

        for (Modules requiredModule : moduleType.getRequiredModules()) {
            changeStatus(requiredModule, newStatus);
        }

        switch (newStatus) {
            case PRE_ENABLE -> {
                if (module.getStatus().equals(ModulePhase.LOADED)) {
                    logger.info("Pre enabling " + moduleType.getName() + " module...");
                    module.onPreEnable();
                    module.setStatus(ModulePhase.PRE_ENABLE);
                }
            }
            case ENABLE -> {
                if (module.getStatus().equals(ModulePhase.PRE_ENABLE)) {
                    logger.info("Enabling " + moduleType.getName() + " module...");
                    module.onEnable();
                    module.setStatus(ModulePhase.ENABLE);
                }
            }
            case POST_ENABLE -> {
                if (module.getStatus().equals(ModulePhase.ENABLE)) {
                    logger.info("Post enabling " + moduleType.getName() + " module...");
                    module.onPostEnable();
                    module.setStatus(ModulePhase.POST_ENABLE);
                }
            }
            case ENABLED -> {
                if (module.getStatus().equals(ModulePhase.POST_ENABLE)) {
                    logger.info("Enabled " + moduleType.getName() + " module...");
                    module.setStatus(ModulePhase.ENABLED);
                }
            }
            case PRE_DISABLE -> {
                if (module.getStatus().equals(ModulePhase.ENABLED)) {
                    logger.info("Pre disabling " + moduleType.getName() + " module...");
                    module.onPreDisable();
                    module.setStatus(ModulePhase.PRE_DISABLE);
                }
            }
            case DISABLE -> {
                if (module.getStatus().equals(ModulePhase.PRE_DISABLE)) {
                    logger.info("Disabling " + moduleType.getName() + " module...");
                    module.onDisable();
                    module.setStatus(ModulePhase.DISABLE);
                }
            }
            case POST_DISABLE -> {
                if (module.getStatus().equals(ModulePhase.DISABLE)) {
                    logger.info("Post disabling " + moduleType.getName() + " module...");
                    module.onPostDisable();
                    module.setStatus(ModulePhase.POST_DISABLE);
                }
            }
            case DISABLED -> {
                if (module.getStatus().equals(ModulePhase.POST_DISABLE)) {
                    logger.info("Disabled " + moduleType.getName() + " module...");
                    module.setStatus(ModulePhase.DISABLED);
                }
            }
        }
    }

    public void unloadModules() {
        logger.info("Unloading modules...");

        logger.info("Pre disabling modules...");
        changeStatuses(ModulePhase.PRE_DISABLE);

        logger.info("Disabling modules...");
        changeStatuses(ModulePhase.DISABLE);

        logger.info("Post disabling modules...");
        changeStatuses(ModulePhase.POST_DISABLE);

        logger.info("Disabled modules...");
        changeStatuses(ModulePhase.DISABLED);

        while (!modules.isEmpty()) {
            // New list to avoid ConcurrentException
            for (Modules module : new LinkedList<>(modules.keySet())) {
                try {
                    unloadModule(module);
                } catch (UnloadModuleException ignored) {
                }
            }
        }

        logger.info("Modules disabled");
    }

    private void unloadModule(Modules moduleType) throws UnloadModuleException {
        // New list to avoid ConcurrentException
        for (Modules mod : new LinkedList<>(modules.keySet())) {
            if (!mod.equals(moduleType))
                for (Modules dependency : mod.getRequiredModules())
                    if (dependency.equals(moduleType))
                        throw new UnloadModuleException(
                                "Cannot unload module because module " + mod + " depend on it...");
        }

        logger.info("Unloading " + moduleType.getName() + " module...");
        modules.remove(moduleType);
    }

    public Module getModule(Modules moduleType) {
        return modules.get(moduleType);
    }
}
