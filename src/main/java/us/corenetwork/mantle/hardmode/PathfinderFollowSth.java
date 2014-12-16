package us.corenetwork.mantle.hardmode;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import us.corenetwork.mantle.MLog;


/**
 * Created by Ginaf on 2014-12-14.
 */
public class PathfinderFollowSth extends AbstractWitherMove {

    private CustomWither wither;
    private boolean running;
    private int countdown;
    private int move;

    private TargetEntity targetEntity;


    public PathfinderFollowSth(CustomWither wither)
    {
        super(wither);
        this.wither = wither;
        this.a(7);
    }

    @Override
    public boolean a()
    {
        if(wither.bb().nextInt(100) < 20)
        {
            return true;
        }
        return false;
    }


    @Override
    public boolean b()
    {
        return countdown > 0;
    }

    @Override
    public void c()
    {
        MLog.debug("[WITHER] Starting FollowSth");
        running = true;
        countdown = 500;
        move = 0;
        //TODO remove debug
        targetEntity = new TargetEntity(wither.world, wither.locX, wither.locY, wither.locZ);

        wither.world.addEntity(targetEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        wither.setGoalTarget(targetEntity, EntityTargetEvent.TargetReason.CUSTOM, false);

    }

    @Override
    public void d()
    {
        MLog.debug("[WITHER] Stopping FollowSth");
        running = false;
        wither.setGoalTarget(null);
        wither.world.removeEntity(targetEntity);
    }

    @Override
    public void e()
    {
        --countdown;
        if(wither.world.getTime() % 100 == 0)
        {
            switch (move) {
                case 0:
                    targetEntity.locX += 10;
                    targetEntity.locY += 1;
                    targetEntity.locZ += 10;
                    break;
                case 1:
                    targetEntity.locX += -10;
                    targetEntity.locY += 2;
                    targetEntity.locZ += 0;
                    break;
                case 2:
                    targetEntity.locX += 0;
                    targetEntity.locY += 1;
                    targetEntity.locZ += -10;
                    break;
                case 3:
                    targetEntity.locX += 0;
                    targetEntity.locY += -4;
                    targetEntity.locZ += 0;
                    break;


            }
            move++;
        }
    }

    @Override
    public boolean i()
    {
        return !running;
    }
}
