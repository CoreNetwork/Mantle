package us.corenetwork.mantle.hardmode.animals;

import net.minecraft.server.v1_8_R3.EntityChicken;
import net.minecraft.server.v1_8_R3.World;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

public class CustomChicken extends EntityChicken
{
    public CustomChicken(World world)
    {
        super(world);
        NearbyPlayerPathfinderGoalProxy.apply(this, HardmodeSettings.ANIMALS_ENABLE_AI_NERF);
    }
}
