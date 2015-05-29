package us.corenetwork.mantle.hardmode.animals;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import net.minecraft.server.v1_8_R3.PathfinderGoalBreed;
import net.minecraft.server.v1_8_R3.PathfinderGoalEatTile;
import net.minecraft.server.v1_8_R3.PathfinderGoalFloat;
import net.minecraft.server.v1_8_R3.PathfinderGoalPanic;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.PathfinderGoalTempt;
import us.corenetwork.mantle.hardmode.HardmodeSettings;
import us.corenetwork.mantle.util.ReflectionUtils;

/**
 * Proxy for PathfinderGoal that will disable it if there is no player nearby
 */
public class NearbyPlayerPathfinderGoalProxy extends PathfinderGoal
{
    public static double maximumRangeToPlayer;

    private PathfinderGoal original;
    private EntityInsentient entity;

    private HardmodeSettings enableNerfSetting;

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

        if (!enableNerfSetting.bool())
            return true;

        //If original can start, then we should check for nearby player and only start if there is one.
        return entity.world.findNearbyPlayer(entity, maximumRangeToPlayer) != null;
    }

    public NearbyPlayerPathfinderGoalProxy(PathfinderGoal original, EntityInsentient entity, HardmodeSettings enableNerfSetting)
    {
        super();
        this.original = original;
        this.entity = entity;
        this.enableNerfSetting = enableNerfSetting;
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

    public static void apply(EntityInsentient entityLiving, HardmodeSettings enableNerfSetting)
    {
        PathfinderGoalSelector goalSelector = entityLiving.goalSelector;
        List goalsListB = (List) ReflectionUtils.get(PathfinderGoalSelector.class, goalSelector, "b"); //List of all pathfinder goals
        List goalsListC = (List) ReflectionUtils.get(PathfinderGoalSelector.class, goalSelector, "c"); //Cache of pathfinder goals
        Class pathfinderGoalSelectorItemClass = null;
        try
        {
            pathfinderGoalSelectorItemClass = Class.forName("net.minecraft.server.v1_8_R3.PathfinderGoalSelector$PathfinderGoalSelectorItem");
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
            if (originalGoal instanceof PathfinderGoalFloat || originalGoal instanceof PathfinderGoalPanic || originalGoal instanceof PathfinderGoalBreed || originalGoal instanceof PathfinderGoalTempt || originalGoal instanceof PathfinderGoalEatTile)
                continue;

            ReflectionUtils.set(pathfinderGoalSelectorItemClass, pathfinderGoalSelectorItem, "a", new NearbyPlayerPathfinderGoalProxy(originalGoal, entityLiving, enableNerfSetting));
        }
        goalsListC.clear(); //Clear cache
    }
}
