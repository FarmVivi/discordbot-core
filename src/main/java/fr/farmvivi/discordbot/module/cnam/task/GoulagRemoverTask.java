package fr.farmvivi.discordbot.module.cnam.task;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class GoulagRemoverTask implements Runnable {
    private final Role role;
    private final Member member;

    public GoulagRemoverTask(Role role, Member member) {
        this.role = role;
        this.member = member;
    }

    @Override
    public void run() {
        member.getGuild().removeRoleFromMember(member, role).queue();
    }
}
