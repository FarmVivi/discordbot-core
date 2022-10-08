package fr.farmvivi.discordbot.module.general.poll;

import java.util.Set;
import java.util.TreeSet;

public class PollResponse {
    private final int id;
    private final String response;
    private final Set<Long> users = new TreeSet<>();

    public PollResponse(int id, String response) {
        this.id = id;
        this.response = response;
    }

    public int getId() {
        return id;
    }

    public String getResponse() {
        return response;
    }

    public Set<Long> getUsers() {
        return users;
    }
}
