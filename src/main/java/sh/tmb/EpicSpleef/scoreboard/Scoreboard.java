package sh.tmb.EpicSpleef.scoreboard;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import sh.tmb.EpicSpleef.objects.SpleefPlayer;

import java.util.List;

public class Scoreboard {

    private SpleefPlayer sp;
    private ScoreboardProvider provider;
    private org.bukkit.scoreboard.Scoreboard scoreboard;
    private Objective objective;

    public Scoreboard(SpleefPlayer sp, ScoreboardProvider provider) {
        this.sp = sp;
        this.provider = provider;
        createAndAttachScoreboard();
    }

    public void setProvider(ScoreboardProvider provider) {
        this.provider = provider;
        createAndAttachScoreboard();
    }

    public void update() {
        List<String> providerLines = provider.getLines(sp);

        for (int i = 0; i < providerLines.size(); i++) {
            String teamName = provider.getFields().get(i);
            String identifier = provider.fieldToColor(i);

            Team t = scoreboard.getTeam(teamName);
            if (t == null) {
                t = scoreboard.registerNewTeam(teamName);
                t.addEntry(identifier);
                objective.getScore(identifier).setScore(15 - i);
            }
            t.setPrefix(providerLines.get(i));
        }
    }

    private void createAndAttachScoreboard() {
        this.scoreboard = sp.getSpigotPlayer().getServer().getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("EpicSpleef", "dummy");
        objective.setDisplayName(provider.getTitle(sp));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        update();
        sp.getSpigotPlayer().setScoreboard(scoreboard);
    }

}
