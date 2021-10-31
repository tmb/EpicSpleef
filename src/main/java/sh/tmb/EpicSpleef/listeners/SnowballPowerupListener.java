package sh.tmb.EpicSpleef.listeners;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.EpicSpleef;

import java.util.UUID;

public class SnowballPowerupListener implements Listener {

    private EpicSpleef plugin;
    private UUID entityId;
    private int scheduled;

    public SnowballPowerupListener(EpicSpleef plugin, UUID entityId) {
        this.plugin = plugin;
        this.entityId = entityId;
        this.scheduled = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> HandlerList.unregisterAll(this), 20L * 5);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Player && event.getEntity().getUniqueId() == entityId) {
            Player hit = (Player) event.getHitEntity();

            // play hit animation
            hit.playEffect(EntityEffect.HURT);

            // deal knockback
            double speed = .65;
            double x = event.getEntity().getVelocity().getX();
            double z = event.getEntity().getVelocity().getZ();
            double y = 0.3333; // this way, like normal knockback, it hits a player a little bit up
            double multiplier = Math.sqrt((speed*speed) / (x*x + y*y + z*z)); // get a constant that, when multiplied by the vector, results in the speed we want
            hit.setVelocity(new Vector(x, y, z).multiply(multiplier).setY(y));

            Bukkit.getScheduler().cancelTask(scheduled);
            HandlerList.unregisterAll(this);
        }
    }
}
