package fr.farmvivi.discordbot.module.general.poll;

import java.util.Comparator;

public class PollResponseComparator implements Comparator<PollResponse> {
    @Override
    public int compare(PollResponse o1, PollResponse o2) {
        return o2.getUsers().size() - o1.getUsers().size();
    }
}
