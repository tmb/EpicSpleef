package sh.tmb.EpicSpleef.scoreboard.providers;

import org.bukkit.ChatColor;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.objects.SpleefPlayer;
import sh.tmb.EpicSpleef.scoreboard.ScoreboardProvider;

import java.util.ArrayList;
import java.util.List;

public class LobbyProvider extends ScoreboardProvider {

    private GameManager gm;

    public LobbyProvider(GameManager gm) {
        this.gm = gm;
    }

    @Override
    public List<String> getFields() {
        List<String> fields = new ArrayList<>();

        fields.add("welcome");

        return fields;
    }

    @Override
    public List<String> getLines(SpleefPlayer sp) {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.AQUA + "Welcome, " + sp.getSpigotPlayer().getName() + "!");

        return lines;
    }

    @Override
    public String getTitle(SpleefPlayer sp) {
        return ChatColor.BOLD.toString() + ChatColor.RED + "E" + ChatColor.GREEN + "P" + ChatColor.BLUE + "I" + ChatColor.YELLOW + "C" + " " + ChatColor.AQUA + "SPLEEF";
    }
}
