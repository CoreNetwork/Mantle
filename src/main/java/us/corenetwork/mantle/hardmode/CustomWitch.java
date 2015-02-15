package us.corenetwork.mantle.hardmode;

import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_8_R1.EntityWitch;
import net.minecraft.server.v1_8_R1.PathfinderGoal;
import net.minecraft.server.v1_8_R1.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_8_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R1.World;
import us.core_network.cornel.java.ReflectionUtils;

/**
 * Created by Matej on 14.1.2015.
 */
public class CustomWitch extends EntityWitch
{
    private static Class pathfinderGoalSelectorItemClass;

    static
    {
        try
        {
            pathfinderGoalSelectorItemClass = Class.forName("net.minecraft.server.v1_8_R1.PathfinderGoalSelectorItem");
        }
        catch (ClassNotFoundException e1)
        {
            e1.printStackTrace();
        }

    }

    public CustomWitch(World world)
    {
        super(world);

        //Remove vanilla PathfinderGoalArrowAttack
        List goalsListB = (List) ReflectionUtils.get(PathfinderGoalSelector.class, goalSelector, "b");
        Iterator iterator = goalsListB.iterator();
        while (iterator.hasNext())
        {
            Object pathfinderGoalSelectorItem = iterator.next();
            PathfinderGoal goal = (PathfinderGoal) ReflectionUtils.get(pathfinderGoalSelectorItemClass, pathfinderGoalSelectorItem, "a");
            if (goal instanceof PathfinderGoalArrowAttack)
                iterator.remove();
        }

        //Add our own ranged attack pathfinder goal
        this.goalSelector.a(2, new CustomPathfinderGoalArrowAttack(this, 1.0D, 60, 60, 10.0F, HardmodeSettings.WITCH_FIRST_POTION_DELAY_MULTIPLIER));
    }
}
