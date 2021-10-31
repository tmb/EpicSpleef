package sh.tmb.EpicSpleef.powerups;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.objects.Powerup;

import java.util.ArrayList;

public class PlacePowerup extends Powerup {

    public PlacePowerup(EpicSpleef plugin, int id, Location spawnLocation, int countdown) {
        super(plugin, id, "Place", Material.PRISMARINE, ChatColor.YELLOW, spawnLocation, countdown);
    }

    @Override
    public void activate() {
        super.activate();
        Player p = getPlugin().getServer().getPlayer(this.getPlayer());

        Location playerLoc = p.getLocation();
        playerLoc.setY(getPlugin().gm.getMm().getMapPlatformCenter().getY());

        ArrayList<Block> blocks = new ArrayList<>();

        playerLoc.getBlock().setType(getPlugin().gm.getMm().getSpleefBlocks().stream().findFirst().get());

        blocks.add(playerLoc.getBlock().getRelative(BlockFace.NORTH));
        blocks.add(playerLoc.getBlock().getRelative(BlockFace.NORTH_EAST));
        blocks.add(playerLoc.getBlock().getRelative(BlockFace.NORTH_WEST));
        blocks.add(playerLoc.getBlock().getRelative(BlockFace.EAST));
        blocks.add(playerLoc.getBlock().getRelative(BlockFace.WEST));
        blocks.add(playerLoc.getBlock().getRelative(BlockFace.SOUTH_WEST));
        blocks.add(playerLoc.getBlock().getRelative(BlockFace.SOUTH));
        blocks.add(playerLoc.getBlock().getRelative(BlockFace.SOUTH_EAST));

        blocks.forEach((block) -> {
            block.setType(getPlugin().gm.getMm().getSpleefBlocks().stream().findFirst().get());
            getPlugin().getServer().getOnlinePlayers().forEach((pl) -> {
                pl.spawnParticle(Particle.BLOCK_DUST, block.getLocation(), 100, getPlugin().gm.getMm().getSpleefBlocks().stream().findFirst().get().createBlockData());
                pl.playSound(block.getLocation(), Sound.BLOCK_METAL_PLACE, 1F, 1.0F);
            });
        });

    }
}
