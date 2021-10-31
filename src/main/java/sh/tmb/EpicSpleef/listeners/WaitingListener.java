package sh.tmb.EpicSpleef.listeners;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.objects.SpleefPlayer;

public class WaitingListener implements Listener {

    private GameManager gm;

    public WaitingListener(GameManager gm) {
        this.gm = gm;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
            Player p = event.getPlayer();
            gm.joinPlayer(p);
            event.setJoinMessage(ChatColor.DARK_PURPLE + p.getName() + " joined the lobby");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
            Player p = event.getPlayer();
            gm.exitPlayer(event.getPlayer());
            event.setQuitMessage(ChatColor.RED + p.getName() + " left the lobby");
    }


    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) ||!(event.getDamager() instanceof Player)) {
            return;
        }

        SpleefPlayer sp = gm.getSpleefPlayerFromPlayer((Player) event.getEntity());
        if (sp.getMode().equals(SpleefPlayer.SpleefPlayerMode.PLAYER)) {
            Player damager = (Player) event.getDamager();

            double speed = .25;
            double x = damager.getEyeLocation().getDirection().getX();
            double z = damager.getEyeLocation().getDirection().getZ();
            double y = 0.3333; // this way, like normal knockback, it hits a player a little bit up
            double multiplier = Math.sqrt((speed*speed) / (x*x + y*y + z*z)); // get a constant that, when multiplied by the vector, results in the speed we want

            event.getEntity().setVelocity(new Vector(x, y, z).multiply(multiplier).setY(y));
            event.setDamage(0);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFade(BlockFadeEvent event) {
        if (event.getBlock().getType() == Material.TUBE_CORAL)
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }



}
