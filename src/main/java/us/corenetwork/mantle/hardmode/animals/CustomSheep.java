package us.corenetwork.mantle.hardmode.animals;

import net.minecraft.server.v1_8_R1.EntitySheep;
import net.minecraft.server.v1_8_R1.World;

public class CustomSheep extends EntitySheep
{
    public CustomSheep(World world)
    {
        super(world);
        NearbyPlayerPathfinderGoalProxy.apply(this);
    }
}
