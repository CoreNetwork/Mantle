package us.corenetwork.mantle.hardmode.animals;

import net.minecraft.server.v1_8_R1.EntitySheep;
import net.minecraft.server.v1_8_R1.World;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

public class CustomSheep extends EntitySheep
{
    public CustomSheep(World world)
    {
        super(world);
        NearbyPlayerPathfinderGoalProxy.apply(this, HardmodeSettings.ANIMALS_ENABLE_AI_NERF);
    }
}
