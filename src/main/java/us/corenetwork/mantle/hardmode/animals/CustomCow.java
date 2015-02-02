package us.corenetwork.mantle.hardmode.animals;

import net.minecraft.server.v1_8_R1.EntityCow;
import net.minecraft.server.v1_8_R1.World;

public class CustomCow extends EntityCow
{
    public CustomCow(World world)
    {
        super(world);
        NearbyPlayerPathfinderGoalProxy.apply(this);
    }
}
