package us.corenetwork.mantle.hardmode.animals;

import java.util.Iterator;
import java.util.List;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.PathfinderGoal;
import net.minecraft.server.v1_8_R1.PathfinderGoalBreakDoor;
import net.minecraft.server.v1_8_R1.PathfinderGoalBreed;
import net.minecraft.server.v1_8_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_8_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R1.PathfinderGoalTempt;
import us.corenetwork.mantle.util.ReflectionUtils;

/**
 * Proxy for PathfinderGoal that will disable it if there is no player nearby
 */
public class NearbyPlayerPathfinderGoalProxy extends PathfinderGoal
{
    public static double maximumRangeToPlayer;

    private PathfinderGoal original;
    private EntityInsentient entity;

    /**
     * Method that determines whether PathfinderGoal should start executing or not
     * @return <code>true</code> if goal should start executing
     */
    @Override
    public boolean a()
    {
        boolean canOriginalStart = original.a();
        if (!canOriginalStart)
            return false;

        //If original can start, then we should check for nearby player and only start if there is one.
        return entity.world.findNearbyPlayer(entity, maximumRangeToPlayer) != null;
    }

    public NearbyPlayerPathfinderGoalProxy(PathfinderGoal original, EntityInsentient entity)
    {
        super();
        this.original = original;
        this.entity = entity;
    }

    @Override
    public boolean b()
    {
        return original.b();
    }

    @Override
    public boolean i()
    {
        return original.i();
    }

    @Override
    public void c()
    {
        original.c();
    }

    @Override
    public void d()
    {
        original.d();
    }

    @Override
    public void e()
    {
        original.e();
    }

    @Override
    public void a(int i)
    {
        original.a(i);
    }

    @Override
    public int j()
    {
        return original.j();
    }

    public static void apply(EntityInsentient entityLiving)
    {
        PathfinderGoalSelector goalSelector = entityLiving.goalSelector;
        List goalsListB = (List) ReflectionUtils.get(PathfinderGoalSelector.class, goalSelector, "b"); //List of all pathfinder goals
        List goalsListC = (List) ReflectionUtils.get(PathfinderGoalSelector.class, goalSelector, "c"); //Cache of pathfinder goals
        Class pathfinderGoalSelectorItemClass = null;
        try
        {
            pathfinderGoalSelectorItemClass = Class.forName("net.minecraft.server.v1_8_R1.PathfinderGoalSelectorItem");
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        Iterator iterator = goalsListB.iterator();
        while (iterator.hasNext())
        {
            Object pathfinderGoalSelectorItem = iterator.next();
            PathfinderGoal originalGoal = (PathfinderGoal) ReflectionUtils.get(pathfinderGoalSelectorItemClass, pathfinderGoalSelectorItem, "a");

            //Excluded goals
            if (originalGoal instanceof PathfinderGoalFloat || originalGoal instanceof PathfinderGoalPanic || originalGoal instanceof PathfinderGoalBreed || originalGoal instanceof PathfinderGoalTempt)
                continue;

            ReflectionUtils.set(pathfinderGoalSelectorItemClass, pathfinderGoalSelectorItem, "a", new NearbyPlayerPathfinderGoalProxy(originalGoal, entityLiving));
        }
        goalsListC.clear(); //Clear cache
    }
}
