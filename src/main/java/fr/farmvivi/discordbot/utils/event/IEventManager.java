package fr.farmvivi.discordbot.utils.event;

public interface IEventManager {
    void register(Object listener);

    void unregister(Object listener);

    void handle(IEvent event);

    void handleAsync(IEvent event);
}
