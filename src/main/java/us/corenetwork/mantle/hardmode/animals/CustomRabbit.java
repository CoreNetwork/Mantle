package us.corenetwork.mantle.hardmode.animals;

import net.minecraft.server.v1_8_R1.EntityRabbit;
import net.minecraft.server.v1_8_R1.World;

public class CustomRabbit extends EntityRabbit
{
    public CustomRabbit(World world)
    {
        super(world);
        NearbyPlayerPathfinderGoalProxy.apply(this);
    }
}
