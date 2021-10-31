package sh.tmb.EpicSpleef.entities;

import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntitySnowball;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.World;

import java.util.UUID;

public class TNTProjectile extends EntitySnowball {

    private UUID thrownBy;

    public TNTProjectile(World world, UUID thrownBy) {
        super(EntityTypes.SNOWBALL, world);
        this.thrownBy = thrownBy;
    }

    public UUID getThrownBy() {
        return thrownBy;
    }

    public void setThrownBy(UUID thrownBy) {
        this.thrownBy = thrownBy;
    }

    // this method is actually canHitEntity — and so we are saying no it cannot hit any entity ever
    @Override
    public boolean a(Entity e) {
        return false;
    }

}
