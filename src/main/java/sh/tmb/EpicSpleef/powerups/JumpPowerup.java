package sh.tmb.EpicSpleef.powerups;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.objects.Powerup;

import java.util.ArrayList;

public class JumpPowerup extends Powerup {

    public JumpPowerup(EpicSpleef plugin, int id, Location spawnLocation, int countdown) {
        super(plugin, id, "Jump", Material.GOLDEN_BOOTS, ChatColor.YELLOW, spawnLocation, countdown);
    }

    @Override
    public void activate() {
        super.activate();
        Player p = getPlugin().getServer().getPlayer(this.getPlayer());
        p.setVelocity(new Vector(p.getVelocity().getX(), 1.25, p.getVelocity().getZ()));
        getPlugin().getServer().getOnlinePlayers().forEach((pl) -> pl.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1L, 1L));
    }
}
