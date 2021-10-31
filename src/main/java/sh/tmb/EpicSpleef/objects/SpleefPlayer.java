package sh.tmb.EpicSpleef.objects;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.scoreboard.Scoreboard;

import java.util.UUID;

public class SpleefPlayer {

    private UUID player;
    private UUID spleefedBy = null;
    private GameManager gm;
    private SpleefPlayerMode mode;
    private Integer kills = 0;
    private Integer powerupsUsed = 0;
    private Integer blocksBroken = 0;
    private Integer powerupId = null;
    private boolean winner = false;
    private boolean dead = false;
    private Scoreboard scoreboard;

    public SpleefPlayer(Player p, SpleefPlayerMode mode, GameManager gm) {
        this.gm = gm;
        this.player = p.getUniqueId();
        this.scoreboard = new Scoreboard(this, gm.getScoreboardProvider());
        setMode(mode);
    }

    public void assignPowerup(int powerupId) throws Exception {
        gm.pm.assignPowerup(powerupId, getSpigotPlayer());
        this.powerupId = powerupId;
    }

    public void activatePowerup() {
        if (hasPowerup()) {
            gm.pm.activatePowerup(powerupId);
            powerupId = null;
        }

    }

    public boolean hasPowerup() {
        return powerupId != null;
    }

    public Player getSpigotPlayer() {
        return gm.plugin.getServer().getPlayer(player);
    }

    public UUID getPlayer() {
        return player;
    }

    public Integer getKills() {
        return kills;
    }

    public SpleefPlayerMode getMode() {
        return mode;
    }

    public void setSpleefedBy(SpleefPlayer sp) {
        spleefedBy = sp.getPlayer();
    }

    public UUID getSpleefedBy() {
        return spleefedBy;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public void killPlayer() {
        if (mode != SpleefPlayerMode.PLAYER) {
            // better errors in the future. for now just soft fail.
            return;
        }

        this.setMode(SpleefPlayer.SpleefPlayerMode.PLAYER_DEAD);
        String killedBy = getSpleefedBy() == null ? "themselves" : gm.getSpleefPlayerFromUUID(getSpleefedBy()).getSpigotPlayer().getName();
        gm.plugin.getServer().broadcastMessage(ChatColor.RED + getSpigotPlayer().getName() + ChatColor.WHITE + " was spleefed by " + ChatColor.RED + killedBy);
        getSpigotPlayer().setFlying(true);
        getSpigotPlayer().teleport(gm.getMm().getSpawn().add(0, 5, 0));
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }

    public void setMode(SpleefPlayerMode mode)  {
        if (mode == SpleefPlayerMode.PLAYER_DEAD && this.mode != SpleefPlayerMode.PLAYER) {
            return;
        }

        this.mode = mode;

        Player p = getSpigotPlayer();

        switch(mode) {
            case PLAYER:
                p.setGameMode(GameMode.ADVENTURE);
                p.setFlying(false);
                p.setAllowFlight(false);
                // if you're a player, everyone can see you.
                gm.getUsers().forEach((pl) -> pl.getSpigotPlayer().showPlayer(gm.plugin, p));
                break;
            case SPECTATOR:
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);
                p.sendMessage(ChatColor.YELLOW + "You are now spectating!");
                p.getInventory().clear();
                // if you're a spectator, no one can see you.
                gm.getUsers().forEach((pl) -> pl.getSpigotPlayer().hidePlayer(gm.plugin, p));
                break;
            case PLAYER_DEAD:
                setDead(true);
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);
                p.sendMessage(ChatColor.YELLOW + "You are now spectating!");
                p.getInventory().clear();
                // if you're a spectator, no one can see you.
                gm.getUsers().forEach((pl) -> pl.getSpigotPlayer().hidePlayer(gm.plugin, p));
                break;
        }
    }

    public Integer getPowerupsUsed() {
        return powerupsUsed;
    }

    public void addKill(int kills) {
        this.kills += kills;
    }

    public void addPowerupsUsed(int powerupsUsed) {
        this.powerupsUsed += powerupsUsed;
    }

    // reset kills, powerups used, powerup id, spleefed by, winner, dead
    public void resetPlayer() {
        System.out.println("resetting powerupId for " + getSpigotPlayer().getName());
        kills = 0;
        powerupsUsed = 0;
        powerupId = null;
        spleefedBy = null;
        winner = false;
        dead = false;
    }

    public Integer getBlocksBroken() {
        return blocksBroken;
    }

    public void addBlocksBroken(Integer blocksBroken) {
        this.blocksBroken += blocksBroken;
    }

    public Scoreboard getScoreboard() { return scoreboard; }

    public enum SpleefPlayerMode {
        PLAYER,
        PLAYER_DEAD,
        SPECTATOR
    }
}
