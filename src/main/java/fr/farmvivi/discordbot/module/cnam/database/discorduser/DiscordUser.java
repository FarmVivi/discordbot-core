package fr.farmvivi.discordbot.module.cnam.database.discorduser;

import java.util.Objects;

public class DiscordUser {
    private final long id;

    public DiscordUser(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return id + "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordUser discordUser = (DiscordUser) o;
        return id != -1 && id == discordUser.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
