package sh.tmb.EpicSpleef.managers;

import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.configuration.MemorySection;
import sh.tmb.EpicSpleef.EpicSpleef;
import sh.tmb.EpicSpleef.managers.GameManager;
import sh.tmb.EpicSpleef.objects.Map;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapManager {

    private GameManager gm;
    private EpicSpleef plugin;
    private HashMap<String, sh.tmb.EpicSpleef.objects.Map> maps = new HashMap<>();
    private sh.tmb.EpicSpleef.objects.Map activeMap = null;
    private World world;

    public MapManager(GameManager gm) {
        this.gm = gm;
        this.plugin = gm.plugin;
    }

    public void intialize() {
        plugin.getConfig().getConfigurationSection("maps").getValues(false).forEach((name, serializedMap) -> {
            MemorySection map = (MemorySection) serializedMap;

            ArrayList<Material> spleefBlocks = new ArrayList<>();

            List<String> spleefBlocksString = map.getStringList("spleef-blocks");
            spleefBlocksString.forEach((s) -> spleefBlocks.add(Material.valueOf(s)));

            maps.put(name,
                new sh.tmb.EpicSpleef.objects.Map(
                    name,
                    map.getInt("x"),
                    map.getInt("y"),
                    map.getInt("z"),
                    map.getInt("radius"),
                    spleefBlocks
                )
            );
        });
    }

    public boolean loadMap(String mapName, Runnable after) {
        if (activeMap != null) {
            return false;
        }

        if (!maps.containsKey(mapName)) {
            return false;
        }

        Map m = maps.get(mapName);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, (k) -> {
            File template = new File("./spleef_worlds/" + m.getWorldName());
            File moveTo = new File("./minigame");

            try {
                FileUtils.copyDirectory(template, moveTo);
            } catch (IOException e) {
                e.printStackTrace();
            }

            plugin.getLogger().info("finished copying!");

            Bukkit.getScheduler().runTask(plugin, () -> {
                WorldCreator wc = new WorldCreator("minigame");
                World loaded = wc.createWorld();
                world = loaded;
                world.setAutoSave(false);
                world.setKeepSpawnInMemory(false);
                loaded.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                loaded.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                loaded.setGameRule(GameRule.DO_TILE_DROPS, false);
                loaded.setTime(1000);
                after.run();
            });
        });

        activeMap = m;
        return true;
    }

    public boolean loadRandomMap(String except, Runnable after) {
        Random r = new Random();
        String mapName = getMaps().get(r.nextInt(getMaps().size()));
        while (mapName.equals(except)) {
            mapName = getMaps().get(r.nextInt(getMaps().size()));
        }

        return loadMap(mapName, after);
    }


    public boolean loadRandomMap(Runnable after) {
        Random r = new Random();
        String mapName = getMaps().get(r.nextInt(getMaps().size()));
        return loadMap(mapName, after);
    }

    public boolean hasMap() {
        return activeMap != null;
    }

    public String getMapName() {
        return activeMap.getName();
    }

    public List<String> getMaps() {
        return new ArrayList<>(maps.keySet());
    }

    public boolean unloadMap(Runnable after) {
        if (activeMap == null) {
            return false;
        }
        // unload world
        Bukkit.unloadWorld(world, false);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // delete folder
            File minigame = new File("./minigame");
            try {
                FileUtils.deleteDirectory(minigame);
            } catch (IOException e) {
                e.printStackTrace();
            }

            plugin.getLogger().info("finished deleting!");

            after.run();
        });
        activeMap = null;
        world = null;
        return true;
    }

    public int getMapRadius() {
        return activeMap.getRadius();
    }

    public World getWorld() { return world; }

    public Location getSpawn() { return activeMap.getSpawn(world); }

    public Location getMapPlatformCenter() { return activeMap.getSpawn(world).subtract(0, 1, 0); }

    public Collection<Material> getSpleefBlocks() {
        return activeMap.getSpleefBlocks();
    }

    private void setWorld(World world) {
        this.world = world;
    }


}
