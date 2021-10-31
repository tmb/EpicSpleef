package sh.tmb.EpicSpleef;

import org.bukkit.plugin.java.JavaPlugin;
import sh.tmb.EpicSpleef.commands.ControlCommand;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.utilities.CircleGenerator;

public class EpicSpleef extends JavaPlugin {

    public GameManager gm;

    @Override
    public void onEnable() {
        getServer().getLogger().info("EpicSpleef activated.");

        this.saveDefaultConfig();

        gm = new GameManager(this);
        gm.setup();
        CircleGenerator.generateCache();

        getServer().getPluginCommand("control").setExecutor(new ControlCommand(this));

    }

    @Override
    public void onDisable() {
        gm.disable();
        getServer().getLogger().info("EpicSpleef deactivated.");
    }
}
