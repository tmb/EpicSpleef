package sh.tmb.EpicSpleef.powerups;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.objects.Powerup;

public class TeleportPowerup extends Powerup {

    public TeleportPowerup(EpicSpleef plugin, int id, Location spawnLocation, int countdown) {
        super(plugin, id, "Teleport", Material.ENDER_PEARL, ChatColor.DARK_PURPLE, spawnLocation, countdown);
    }

    @Override
    public void activate() {
        super.activate();
        Player p = getPlugin().getServer().getPlayer(this.getPlayer());

        Location playerLoc = p.getLocation();
        getPlugin().getServer().getOnlinePlayers().forEach((pl) -> pl.spawnParticle(Particle.PORTAL, playerLoc, 120));
        getPlugin().getServer().getOnlinePlayers().forEach((pl) -> pl.playSound(playerLoc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1F, 1.0F));
        Vector eyeDirection = p.getEyeLocation().getDirection().normalize().multiply(2.5);

        playerLoc.add(eyeDirection);
        if (playerLoc.getY() < getPlugin().gm.getMm().getSpawn().getY()) {
            playerLoc.add(0, getPlugin().gm.getMm().getSpawn().getY() - playerLoc.getY(), 0);
        }
        p.teleport(playerLoc);
    }

}
