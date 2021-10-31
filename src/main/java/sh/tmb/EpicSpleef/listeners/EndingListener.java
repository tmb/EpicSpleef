package sh.tmb.EpicSpleef.listeners;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import sh.tmb.EpicSpleef.managers.GameManager;

public class EndingListener implements Listener {

    private GameManager gm;

    public EndingListener(GameManager gm) {
        this.gm = gm;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) ||!(event.getDamager() instanceof Player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onReachVoid(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            return;
        }

        Player p = (Player) event.getEntity();
        p.teleport(gm.getMm().getSpawn());
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        gm.joinPlayer(p);
        event.setJoinMessage(net.md_5.bungee.api.ChatColor.DARK_PURPLE + p.getName() + " joined the lobby");
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        gm.exitPlayer(event.getPlayer());
        event.setQuitMessage(net.md_5.bungee.api.ChatColor.RED + p.getName() + " left the lobby");
    }
}
