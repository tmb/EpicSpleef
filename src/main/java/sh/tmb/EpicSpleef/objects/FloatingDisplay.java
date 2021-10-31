package sh.tmb.EpicSpleef.objects;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import sh.tmb.EpicSpleef.EpicSpleef;

public class FloatingDisplay {

    private ArmorStand entity;
    private EpicSpleef plugin;
    private Location loc;

    public FloatingDisplay(EpicSpleef plugin, String text, Location loc) {
        this.loc = loc;
        entity = (ArmorStand) loc.getWorld().spawnEntity(this.loc, EntityType.ARMOR_STAND);
        entity.setCollidable(false);
        entity.setSmall(true);
        entity.setArms(false);
        entity.setVisible(false);
        entity.setInvulnerable(true);
        entity.setCustomNameVisible(true);
        entity.setGravity(false);
        entity.setVelocity(new Vector());
        entity.setCustomName(text);

    }

    public void setText(String text) {
        entity.setCustomName(text);
    }

    public void remove() {
        entity.setCustomNameVisible(false);
        entity.remove();
    }
}
