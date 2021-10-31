package sh.tmb.EpicSpleef.powerups;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.entities.TNTProjectile;
import sh.tmb.EpicSpleef.listeners.BombPowerupListener;
import sh.tmb.EpicSpleef.objects.Powerup;

public class BombPowerup extends Powerup {

    public BombPowerup(EpicSpleef plugin, int id, Location spawnLocation, int countdown) {
        super(plugin, id, "Bomb", Material.TNT, ChatColor.RED, spawnLocation, countdown);
    }

    @Override
    public void activate() {
        super.activate();
        Player p = getPlugin().getServer().getPlayer(this.getPlayer());
        ItemStack i = this.getItem();

        // generate velocity
        Vector eyeDirection = p.getEyeLocation().getDirection().normalize();

        TNTProjectile tntP = new TNTProjectile(((CraftWorld)p.getWorld()).getHandle(), p.getUniqueId());

        tntP.shoot(eyeDirection.getX(), eyeDirection.getY(), eyeDirection.getZ(), 1, 1);
        tntP.setPositionRotation(p.getEyeLocation().getX(), p.getEyeLocation().getY(), p.getEyeLocation().getZ(), 0, 0);
        tntP.setItem(CraftItemStack.asNMSCopy(i));
        ((CraftWorld)p.getWorld()).getHandle().addEntity(tntP, CreatureSpawnEvent.SpawnReason.CUSTOM);

        // bomb powerup listener that de-registers itself after being called
        getPlugin().getServer().getPluginManager().registerEvents(new BombPowerupListener(tntP, i, getPlugin()), getPlugin());
    }
}
