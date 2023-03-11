package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.task.GoulagRemoverTask;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GoulagRemoverEventHandler extends ListenerAdapter {
    private final ScheduledExecutorService scheduler;
    private final Role role;
    private final CoursDAO coursDAO;

    public GoulagRemoverEventHandler(ScheduledExecutorService scheduler, Role role, DatabaseAccess databaseAccess) {
        this.scheduler = scheduler;
        this.role = role;
        this.coursDAO = new CoursDAO(databaseAccess);
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Member member = event.getMember();
        if (event.getRoles().contains(role)) {
            // Now
            LocalDate date = LocalDate.now();
            LocalTime time = LocalTime.now();

            try {
                List<Cours> courss = coursDAO.selectAllByDateBetweenHeure(date, time);
                if (courss.isEmpty()) {
                    event.getGuild().removeRoleFromMember(member, role).queue();
                } else {
                    Cours cours = courss.get(0);

                    // Schedule task to remove role from member after the end of the cours
                    GoulagRemoverTask task = new GoulagRemoverTask(role, member);
                    scheduler.schedule(task, cours.getHeureFin().toSecondOfDay() - time.toSecondOfDay(), TimeUnit.SECONDS);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
