package us.corenetwork.mantle.hardmode.animals;

import net.minecraft.server.v1_8_R1.EntityChicken;
import net.minecraft.server.v1_8_R1.World;

public class CustomChicken extends EntityChicken
{
    public CustomChicken(World world)
    {
        super(world);
        NearbyPlayerPathfinderGoalProxy.apply(this);
    }
}
