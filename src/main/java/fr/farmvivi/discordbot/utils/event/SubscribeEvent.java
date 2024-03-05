package fr.farmvivi.discordbot.utils.event;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SubscribeEvent {
}