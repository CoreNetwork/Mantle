package us.corenetwork.mantle.hardmode.animals;

import net.minecraft.server.v1_8_R1.EntityPig;
import net.minecraft.server.v1_8_R1.World;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

public class CustomPig extends EntityPig
{
    public CustomPig(World world)
    {
        super(world);
        NearbyPlayerPathfinderGoalProxy.apply(this, HardmodeSettings.ANIMALS_ENABLE_AI_NERF);
    }


}
