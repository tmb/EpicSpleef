package sh.tmb.EpicSpleef.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.entities.TNTProjectile;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.utilities.CircleGenerator;

import java.util.HashSet;

public class BombPowerupListener implements Listener {

    private TNTProjectile snowball;
    private ItemStack itemToSpawn;
    private EpicSpleef plugin;
    private int scheduled;

    public BombPowerupListener(TNTProjectile snowball, ItemStack itemToSpawn, EpicSpleef plugin) {
        this.snowball = snowball;
        this.itemToSpawn = itemToSpawn;
        this.plugin = plugin;
        this.scheduled = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> ProjectileHitEvent.getHandlerList().unregister(this), 20L * 10);
    }

    @EventHandler
    public void onMove(ProjectileHitEvent event) {
        if (snowball.getUniqueID() == event.getEntity().getUniqueId()) {
            event.getEntity().getServer().getLogger().info("Snowball hit the ground at " + event.getHitBlock().getLocation());

            Location explosionCenter = event.getHitBlock().getLocation().clone().add(.5, 0, .5);
            Location bombSpawnLoc = event.getHitBlock().getLocation().clone();
            bombSpawnLoc.setX(snowball.locX());
            bombSpawnLoc.setZ(snowball.locZ());
            bombSpawnLoc.add(0, 1, 0);

            Item entity = event.getEntity().getWorld().dropItem(bombSpawnLoc, itemToSpawn);

            entity.setPickupDelay(1000000);
            entity.setInvulnerable(true);
            entity.setVelocity(new Vector());
            entity.setGravity(false);

            Sound sound = Sound.BLOCK_NOTE_BLOCK_BASS;
            float vol = 1.5F;

            plugin.gm.getUsers().forEach((sp) -> sp.getSpigotPlayer().playSound(bombSpawnLoc, sound, vol, 1));
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.gm.getUsers().forEach((sp) -> sp.getSpigotPlayer().playSound(bombSpawnLoc, sound, vol, 1.3F)), 2L);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.gm.getUsers().forEach((sp) -> sp.getSpigotPlayer().playSound(bombSpawnLoc, sound, vol, 1.5F)), 4L);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.gm.getUsers().forEach((sp) -> sp.getSpigotPlayer().playSound(bombSpawnLoc, sound, vol, 1.7F)), 5L);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.gm.getUsers().forEach((sp) -> sp.getSpigotPlayer().playSound(bombSpawnLoc, sound, vol, 1.9F)), 6L);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                entity.remove();
                if (plugin.gm.getStatus() == GameManager.GameStatus.PLAYING) {
                    HashSet<Location> locs = CircleGenerator.generateCircle(explosionCenter, 2, CircleGenerator.Plane.XZ, false, true);
                    locs.add(explosionCenter);
                    plugin.gm.getUsers().forEach((sp) -> sp.getSpigotPlayer().playSound(bombSpawnLoc, sound, vol, 2F));
                    entity.getWorld().createExplosion(bombSpawnLoc, 3, false, false);
                    locs.forEach(loc -> {
                        if (plugin.gm.getMm().getSpleefBlocks().contains(loc.getBlock().getType())) {
                            plugin.gm.setBlockBroken(loc.getBlock().getLocation(), snowball.getThrownBy());
                            loc.getBlock().setType(Material.AIR);
                        }
                    });
                }
            }, 7L);
        }

        Bukkit.getScheduler().cancelTask(scheduled);
        ProjectileHitEvent.getHandlerList().unregister(this);
    }
}
