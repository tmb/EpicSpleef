package sh.tmb.EpicSpleef.scoreboard;

import org.bukkit.ChatColor;
import sh.tmb.EpicSpleef.objects.SpleefPlayer;

import java.util.List;

public abstract class ScoreboardProvider {

    public String fieldToColor(int field) {
        switch (field) {
            case 15:
                return ChatColor.YELLOW.toString();
            case 14:
                return ChatColor.AQUA.toString();
            case 13:
                return ChatColor.BLUE.toString();
            case 12:
                return ChatColor.GREEN.toString();
            case 11:
                return ChatColor.RED.toString();
            case 10:
                return ChatColor.GRAY.toString();
            case 9:
                return ChatColor.LIGHT_PURPLE.toString();
            case 8:
                return ChatColor.WHITE.toString();
            case 7:
                return ChatColor.GOLD.toString();
            case 6:
                return ChatColor.BLACK.toString();
            case 5:
                return ChatColor.DARK_AQUA.toString();
            case 4:
                return ChatColor.DARK_BLUE.toString();
            case 3:
                return ChatColor.DARK_GRAY.toString();
            case 2:
                return ChatColor.DARK_GREEN.toString();
            case 1:
                return ChatColor.DARK_PURPLE.toString();
            case 0:
                return ChatColor.DARK_RED.toString();
            default:
                return null;
        }
    }

    public abstract List<String> getFields();

    public abstract List<String> getLines(SpleefPlayer sp);

    public abstract String getTitle(SpleefPlayer sp);
}
