package sh.tmb.EpicSpleef.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Collection;

public class Map {
    private String worldName;
    private String name;
    private int x;
    private int y;
    private int z;
    private int radius;
    private Collection<Material> spleefBlocks;

    public Map(String worldName, int x, int y, int z, int radius, Collection<Material> spleefBlocks) {
        this.worldName = "spleef_" + worldName;
        this.name = worldName;
        this.x = x;
        this.y= y;
        this.z = z;
        this.radius = radius;
        this.spleefBlocks = spleefBlocks;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getName() {
        return name;
    }


    public Location getSpawn(World w) {
        return new Location(w, x, y, z);
    }

    public int getRadius() {
        return radius;
    }

    public Collection<Material> getSpleefBlocks() {
        return spleefBlocks;
    }
}
