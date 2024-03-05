package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.module.cnam.database.DatabaseManager;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.task.GoulagRemoverTask;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GoulagRemoverEventHandler extends ListenerAdapter {
    private final ScheduledExecutorService scheduler;
    private final Role role;
    private final CoursDAO coursDAO;

    public GoulagRemoverEventHandler(ScheduledExecutorService scheduler, Role role, DatabaseManager databaseManager) {
        this.scheduler = scheduler;
        this.role = role;
        this.coursDAO = new CoursDAO(databaseManager.getDatabaseAccess());
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Member member = event.getMember();
        if (event.getRoles().contains(role)) {
            // Now
            LocalDateTime now = LocalDateTime.now();

            try {
                List<Cours> courss = coursDAO.selectAllByDateTime(now);
                if (courss.isEmpty()) {
                    event.getGuild().removeRoleFromMember(member, role).queue();
                } else {
                    Cours cours = courss.get(0);

                    // Schedule task to remove role from member after the end of the cours
                    GoulagRemoverTask task = new GoulagRemoverTask(role, member);
                    scheduler.schedule(task, cours.getFinCours().toLocalTime().toSecondOfDay() - now.toLocalTime().toSecondOfDay(), TimeUnit.SECONDS);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
