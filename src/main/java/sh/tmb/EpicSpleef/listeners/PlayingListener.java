package sh.tmb.EpicSpleef.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.objects.SpleefPlayer;

import java.util.*;

public class PlayingListener implements Listener {

    private GameManager gm;
    private HashMap<UUID, Long> powerupWarningCooldown;

    public PlayingListener(GameManager gm) {
        this.gm = gm;
        this.powerupWarningCooldown = new HashMap<>();
    }


    @EventHandler
    public void onBlockHit(BlockDamageEvent event) {
        SpleefPlayer sp = gm.getSpleefPlayerFromPlayer(event.getPlayer());

        if (!checkIfPlayer(sp)) {
            event.setCancelled(true);
            return;
        }

        if (gm.getMm().getSpleefBlocks().contains(event.getBlock().getType()) && event.getItemInHand().getType() == Material.DIAMOND_SHOVEL) {
            Player p = event.getPlayer();
            event.setInstaBreak(true);
            gm.setBlockBroken(event.getBlock().getLocation(), sp.getPlayer());
            p.getServer().getOnlinePlayers().forEach((pl) -> pl.playSound(event.getBlock().getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1));
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        SpleefPlayer sp = gm.getSpleefPlayerFromPlayer(event.getPlayer());

        if (!checkIfPlayer(sp)) {
            return;
        }

        // if the player is coming from above platform level (player from y > mapPlatform Y)
        // and they are going to land at platform level (player to y is less than spawn y, but greater than platform y)
        // then set the brokenBy info
        double platformY = gm.getMm().getMapPlatformCenter().getY();
        double spawnY = gm.getMm().getSpawn().getY();

        double fromY = event.getFrom().getY();
        double toY = event.getTo().getY();

        if (fromY > platformY && (toY < spawnY && toY > platformY)) {
            // location of the block they're going to — which is going to be at 26 (platform-level)
            UUID brokenBy = gm.getBlockBrokenBy(event.getTo().getBlock().getLocation());

            if (brokenBy != null) {
                sp.setSpleefedBy(gm.getSpleefPlayerFromUUID(brokenBy));
            }
        }

        Material toType = event.getTo().getBlock().getRelative(BlockFace.DOWN).getType();
        if (toType == Material.AIR)
            toType = event.getTo().getBlock().getType();

        if (!(toType == Material.AIR || toType == Material.VOID_AIR|| gm.getMm().getSpleefBlocks().contains(toType))) {
            Block teleportTo = findNearestBlockOfMaterials(event.getTo().getBlock().getLocation(), gm.getMm().getSpleefBlocks());
            sp.getSpigotPlayer().teleport(teleportTo.getLocation().add(.5, 1, .5));
        }
    }

    @EventHandler
    public void onReachVoid(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID || event.getEntityType() != EntityType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        SpleefPlayer sp = gm.getSpleefPlayerFromPlayer((Player) event.getEntity());

        if (!checkIfPlayer(sp)) {
            event.setCancelled(true);
            sp.getSpigotPlayer().teleport(gm.getMm().getSpawn());
            return;
        }

        event.setDamage(0);
        sp.killPlayer();
        if (sp.getSpleefedBy() != null) {
            SpleefPlayer killer = gm.getSpleefPlayerFromUUID(sp.getSpleefedBy());
            killer.addKill(1);
            sp.getScoreboard().update();
            killer.getScoreboard().update();
        }
        List<SpleefPlayer> cp = gm.getPlayers();

        if (cp.size() <= 1) {
            if (cp.size() == 1) {
                // declare the one remaining player the winner
                gm.setWinner(cp.get(0).getPlayer());
            }

            if (gm.getPlayers().size() == 0) {
                // declare the just perished player the winner
                gm.setWinner(sp.getPlayer());
            }
            gm.setStatus(GameManager.GameStatus.ENDING);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        SpleefPlayer sp = gm.getSpleefPlayerFromPlayer(event.getPlayer());

        if (sp.getMode() == SpleefPlayer.SpleefPlayerMode.PLAYER && gm.getMm().getSpleefBlocks().contains(event.getBlock().getType()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSwitch(PlayerItemHeldEvent event) {
        SpleefPlayer sp = gm.getSpleefPlayerFromPlayer(event.getPlayer());

        if (!checkIfPlayer(sp)) {
            return;
        }

        if (event.getNewSlot() == 1) {
            if (sp.hasPowerup()) {
                sp.activatePowerup();
                sp.addPowerupsUsed(1);
                sp.getScoreboard().update();
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onEnderPearl(PlayerInteractEvent event) {
        if (event.getMaterial() == Material.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent event) throws Exception {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        SpleefPlayer sp = gm.getSpleefPlayerFromPlayer((Player) event.getEntity());

        if (!checkIfPlayer(sp)) {
            event.setCancelled(true);
            return;
        }

        ItemStack is = event.getItem().getItemStack();
        NamespacedKey nsk = new NamespacedKey(gm.plugin, "powerupId");
        if (is.hasItemMeta() && is.getItemMeta().getPersistentDataContainer().has(nsk, PersistentDataType.INTEGER)) {
            // it's a powerup!
            if (sp.hasPowerup()) {
                // person already has powerup — make sure they're only told once.

                // if the cooldown map either doesn't have a player, or if it's been 3 or more seconds since the last message
                if (!powerupWarningCooldown.containsKey(sp.getPlayer()) || System.currentTimeMillis() - powerupWarningCooldown.get(sp.getPlayer()) >= 3000) {
                    // update the cooldown and tell the player that they already have a powerup
                    powerupWarningCooldown.put(sp.getPlayer(), System.currentTimeMillis());
                    sp.getSpigotPlayer().sendMessage(ChatColor.YELLOW + "You already have a powerup!");
                }
                event.setCancelled(true);
                return;
            }
            int powerupId = is.getItemMeta().getPersistentDataContainer().get(nsk, PersistentDataType.INTEGER);

            if (!gm.pm.powerupAlreadyAssigned(powerupId)) {
                sp.assignPowerup(powerupId);
            }

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

    private Block findNearestBlockOfMaterials(Location loc, Collection<Material> blockType) {
        Block b = null;
        double bDist = 50;

        int radius = 7;
        int
                minX = loc.getBlockX() - radius,
                minZ = loc.getBlockZ() - radius,
                maxX = loc.getBlockX() + radius,
                maxZ = loc.getBlockZ() + radius,
                y = gm.getMm().getMapPlatformCenter().getBlockY();

        for (int x = minX; x <= maxX; x++)
            for (int z = minZ; z <= maxZ; z++) {
                Block block = loc.getWorld().getBlockAt(x, y, z);
                if (blockType.contains(block.getType())) {
                    double dist = loc.distance(block.getLocation());
                    if (dist < bDist) {
                        bDist = dist;
                        b = block;
                    }
                }
            }

        return b;
    }
    private boolean checkIfPlayer(SpleefPlayer sp) {
        return sp.getMode() == SpleefPlayer.SpleefPlayerMode.PLAYER;
    }
}
