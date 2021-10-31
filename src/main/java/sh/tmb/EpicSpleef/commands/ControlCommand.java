package sh.tmb.EpicSpleef.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.managers.PowerupManager;

public class ControlCommand implements CommandExecutor {
    private EpicSpleef plugin;

    public ControlCommand(EpicSpleef plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args[0].equals("setstatus")) {
            plugin.gm.setStatus(GameManager.GameStatus.valueOf(args[1]));
            commandSender.sendMessage("Status set!");
            return true;
        }

        if (args[0].equals("maxplayers")) {
            plugin.gm.setPlayersRequired(Integer.valueOf(args[1]));
            commandSender.sendMessage("Max players set!");
            return true;
        }

        if (args[0].equals("powerup")) {
            plugin.gm.pm.createPowerup(PowerupManager.PowerupType.valueOf(args[1]), ((Player) commandSender).getLocation());
            commandSender.sendMessage("Powerup spawned!");
            return true;
        }

        if (args[0].equals("map")) {
            if (args[1].equals("load")) {
                plugin.gm.getMm().loadMap(args[2], () -> {});
                return true;
            }

            if (args[1].equals("unload")) {
                plugin.gm.getMm().unloadMap(() -> {});
                return true;
            }
        }
        return true;
    }
}
