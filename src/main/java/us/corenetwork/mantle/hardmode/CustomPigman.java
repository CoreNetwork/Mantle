package us.corenetwork.mantle.hardmode;

import java.util.List;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPigZombie;
import net.minecraft.server.v1_8_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.World;
import us.core_network.cornel.java.ReflectionUtils;

/**
 * Created by Matej on 14.1.2015.
 */
public class CustomPigman extends EntityPigZombie
{
    public CustomPigman(World world)
    {
        super(world);
        replaceAI();
    }

    /*
        @return is pigmen angry or not.
     */
    @Override
    public boolean ck()
    {
        return true; //Always angry
    }

    private void replaceAI()
    {
        List goalsListA = (List) ReflectionUtils.get(PathfinderGoalSelector.class, targetSelector, "b");
        List goalsListB = (List) ReflectionUtils.get(PathfinderGoalSelector.class, targetSelector, "c");

        // Clear old pigmen AI
        goalsListA.clear();
        goalsListB.clear();

        // Attach zombie AI
        targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[] { EntityPigZombie.class }));
        targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

}
