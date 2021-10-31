package sh.tmb.EpicSpleef.powerups;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.entities.TNTProjectile;
import sh.tmb.EpicSpleef.listeners.BombPowerupListener;
import sh.tmb.EpicSpleef.listeners.SnowballPowerupListener;
import sh.tmb.EpicSpleef.objects.Powerup;

public class SnowballPowerup extends Powerup {

    public SnowballPowerup(EpicSpleef plugin, int id, Location spawnLocation, int countdown) {
        super(plugin, id, "Snowball", Material.SNOWBALL, ChatColor.WHITE, spawnLocation, countdown);
    }

    @Override
    public void activate() {
        super.activate();
        Player p = getPlugin().getServer().getPlayer(this.getPlayer());

        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                Snowball sb = p.launchProjectile(Snowball.class);
                getPlugin().getServer().getPluginManager().registerEvents(new SnowballPowerupListener(getPlugin(), sb.getUniqueId()), getPlugin());
                getPlugin().gm.getUsers().forEach((sp) -> sp.getSpigotPlayer().playSound(sp.getSpigotPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1F, 1F));
                // snowballs goes straight
                sb.setGravity(false);
            }, 3 * i);
        }

    }
}
