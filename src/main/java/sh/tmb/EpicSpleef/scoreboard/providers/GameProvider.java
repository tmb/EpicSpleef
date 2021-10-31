package sh.tmb.EpicSpleef.scoreboard.providers;

import org.bukkit.ChatColor;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.objects.SpleefPlayer;
import sh.tmb.EpicSpleef.scoreboard.ScoreboardProvider;

import java.util.ArrayList;
import java.util.List;

public class GameProvider extends ScoreboardProvider {

    private GameManager gm;

    public GameProvider(GameManager gm) {
        this.gm = gm;
    }

    public List<String> getFields() {
        List<String> fields = new ArrayList<>();

        fields.add("players");
        fields.add("spacer");
        fields.add("kills");
        fields.add("powerups");

        return fields;
    }

    public List<String> getLines(SpleefPlayer sp) {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.AQUA + "Players left" + ChatColor.GRAY + ": " + ChatColor.YELLOW + gm.getPlayers().size());
        lines.add("");
        lines.add(ChatColor.AQUA + "Kills" + ChatColor.GRAY + ": " + ChatColor.YELLOW + sp.getKills());
        lines.add(ChatColor.AQUA + "Powerups used" + ChatColor.GRAY + ": " + ChatColor.YELLOW + sp.getPowerupsUsed());

        return lines;
    }

    public String getTitle(SpleefPlayer sp) {
        return ChatColor.BOLD.toString() + ChatColor.RED + "E" + ChatColor.GREEN + "P" + ChatColor.BLUE + "I" + ChatColor.YELLOW + "C" + " " + ChatColor.AQUA + "SPLEEF";
    }
}
