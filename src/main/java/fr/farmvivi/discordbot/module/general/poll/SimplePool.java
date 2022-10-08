package fr.farmvivi.discordbot.module.general.poll;

import net.dv8tion.jda.api.entities.Role;

public class SimplePool extends Poll {
    public SimplePool(String question, String... responses) {
        super(question, responses);
    }

    public SimplePool(long timeout, String question, String... responses) {
        super(timeout, question, responses);
    }

    public SimplePool(Role role, String question, String... responses) {
        super(role, question, responses);
    }

    public SimplePool(Role role, long timeout, String question, String... responses) {
        super(role, timeout, question, responses);
    }
}
