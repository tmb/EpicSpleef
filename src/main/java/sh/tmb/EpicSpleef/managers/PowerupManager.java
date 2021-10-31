package sh.tmb.EpicSpleef.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.objects.Powerup;
import sh.tmb.EpicSpleef.powerups.*;

import java.util.*;
import java.util.Map;

public class PowerupManager {

    private HashMap<Integer, Powerup> powerups;
    private ArrayList<UUID> isJumping;
    private EpicSpleef plugin;
    private Random r = new Random();
    // time from powerup spawn in until it becomes available for pick up. right now standarized across all powerups.
    private final int timeTillSpawn = 2;

    public enum PowerupType {
        JUMP,
        PLACE,
        TELEPORT,
        BOMB,
        SNOWBALL
    }

    public PowerupManager(EpicSpleef plugin) {
        powerups = new HashMap<>();
        this.plugin = plugin;
        this.isJumping = new ArrayList<>();
    }

    public int createPowerup(PowerupType type, Location spawnLocation) {
        int id = powerups.size();

        while (powerups.containsKey(id)) {
            id++;
        }
        Powerup powerup = null;

        switch (type) {
            case TELEPORT:
                powerup = new TeleportPowerup(plugin, id, spawnLocation, timeTillSpawn);
                break;
            case PLACE:
                 powerup = new PlacePowerup(plugin, id, spawnLocation, timeTillSpawn);
                 break;
            case JUMP:
                powerup = new JumpPowerup(plugin, id, spawnLocation, timeTillSpawn);
                break;
            case BOMB:
                powerup = new BombPowerup(plugin, id, spawnLocation, timeTillSpawn);
                break;
            case SNOWBALL:
                powerup = new SnowballPowerup(plugin, id, spawnLocation, timeTillSpawn);
                break;
        }

        powerups.put(id, powerup);
        powerup.spawn();
        plugin.getLogger().info("Powerup created");
        return id;
    }

    public int createRandomPowerup(Location spawnLocation) {
        List<PowerupManager.PowerupType> types = Collections.unmodifiableList(Arrays.asList(PowerupManager.PowerupType.values()));
        PowerupManager.PowerupType type = types.get(r.nextInt(types.size()));
        return createPowerup(type, spawnLocation);
    }

    public Integer getPowerupIdByPlayer(Player p) {
        Map.Entry<Integer, Powerup> entry = powerups.entrySet().stream().filter((set) -> {
            Powerup powerup = set.getValue();
            return powerup.getPlayer() != null && powerup.getPlayer().equals(p.getUniqueId());
        }).findFirst().orElse(null);

        return entry == null ? null : entry.getKey();
    }

    public boolean powerupAtLocation(Location loc) {
        return powerups.values().stream().anyMatch((powerup -> powerup.getSpawnLocation().equals(loc)));
    }

    public void removePowerup(int id) {
        powerups.get(id).cleanup();
        powerups.remove(id);
    }

    public void removeAll() {
        powerups.values().forEach(Powerup::cleanup);
        powerups.clear();
    }

    public void activatePowerup(int id) {
        powerups.get(id).activate();
        removePowerup(id);
    }

    public void assignPowerup(int id, Player p) throws Exception {
        powerups.get(id).assignToPlayer(p);
    }

    public boolean powerupAlreadyAssigned(int id) {
        return powerups.get(id).getPlayer() != null;
    }

    public boolean playerHasPowerup(Player p) {
        return powerups.values().stream().anyMatch((powerup -> powerup.getPlayer() != null && powerup.getPlayer().equals(p.getUniqueId())));
    }

    public void setJumping(UUID p) {
        isJumping.add(p);
    }

    public void removeJumping(UUID p) {
        isJumping.remove(p);
    }

    public boolean isJumping(UUID p) {
        return isJumping.contains(p);
    }
}
