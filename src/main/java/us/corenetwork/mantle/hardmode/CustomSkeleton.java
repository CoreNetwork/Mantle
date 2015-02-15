package us.corenetwork.mantle.hardmode;

import java.util.List;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityPigZombie;
import net.minecraft.server.v1_8_R1.EntitySkeleton;
import net.minecraft.server.v1_8_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R1.World;
import us.core_network.cornel.java.ReflectionUtils;

/**
 * Created by Matej on 14.1.2015.
 */
public class CustomSkeleton extends EntitySkeleton
{
    public CustomSkeleton(World world)
    {
        super(world);

        //Replace PathfinderGoalArrowAttack with our custom one which has reduced initial shooting delay
        ReflectionUtils.set(EntitySkeleton.class, this, "b", new CustomPathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F, HardmodeSettings.SKELETON_FIRST_ARROW_DELAY_MULTIPLIER));
    }
}
