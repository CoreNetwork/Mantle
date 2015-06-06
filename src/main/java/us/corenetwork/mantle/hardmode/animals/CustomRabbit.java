package us.corenetwork.mantle.hardmode.animals;

import net.minecraft.server.v1_8_R3.EntityRabbit;
import net.minecraft.server.v1_8_R3.World;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

public class CustomRabbit extends EntityRabbit
{
    public CustomRabbit(World world)
    {
        super(world);
        NearbyPlayerPathfinderGoalProxy.apply(this, HardmodeSettings.ANIMALS_ENABLE_AI_NERF);
    }
}
