package us.corenetwork.mantle.hardmode;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import us.corenetwork.mantle.MLog;

public class PathfinderStationaryArtillery extends AbstractPathfinderGoal {

    private CustomWither wither;
    private World world;

    private final int MIN_VERTICAL = HardmodeSettings.WITHER_PH_SA_MIN_VERTICAL.integer();
    private final int MAX_VERTICAL = HardmodeSettings.WITHER_PH_SA_MAX_VERTICAL.integer();
    private final int MAX_HORIZONTAL = HardmodeSettings.WITHER_PH_SA_MAX_HORIZONTAL.integer();
    private final int MIN_HORIZONTAL = HardmodeSettings.WITHER_PH_SA_MIN_HORIZONTAL.integer();
    private final int MOVE_BASIC_TIME = HardmodeSettings.WITHER_PH_SA_MOVE_BASIC_TIME.integer();
    private final int MOVE_VARIANCE = HardmodeSettings.WITHER_PH_SA_MOVE_TIME_VARIANCE.integer();


    private double startLocX;
    private double startLocZ;

    private long nextMoveTime;

    double groundY;
    double finalHeight;

    private TargetEntity targetEntity;


    public PathfinderStationaryArtillery(CustomWither entityinsentient)
    {
        this.wither = entityinsentient;
        this.a(5);

        //TODO dont hardcode this
        this.world = Bukkit.getWorld("world_nether");
    }

    //can start?
    public boolean a()
    {
        //dont start if has target or if during startup phase
        return wither.s(0) == 0 && wither.isInSpawningPhase() == false ;
    }

    //Should continue
    public boolean b()
    {
        //No logic for stopping other than interrupting yet
        return true;
    }

    //Init phase
    @Override
    public void c()
    {
        MLog.debug("[WITHER] Starting Stationary Artillery");
        determineGroundLevel(wither.locX, wither.locZ);
        finalHeight = groundY + MIN_VERTICAL;

        targetEntity = new TargetEntity(wither.world, wither.locX, wither.locY, wither.locZ);

        wither.world.addEntity(targetEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        wither.setGoalTarget(targetEntity, EntityTargetEvent.TargetReason.CUSTOM, false);

        startLocX = wither.locX;
        startLocZ = wither.locZ;

        nextMoveTime = wither.world.getTime() + MOVE_BASIC_TIME + wither.bb().nextInt(2*MOVE_VARIANCE)-MOVE_VARIANCE;
    }


    //Reset phase
    @Override
    public void d()
    {
        MLog.debug("[WITHER] Stopping Stationary Artillery");

        wither.setGoalTarget(null);
        wither.world.removeEntity(targetEntity);

    }

    //Run phase (each tick)
    @Override
    public void e()
    {
        if(wither.world.getTime() > nextMoveTime)
        {
            nextMoveTime = wither.world.getTime() + MOVE_BASIC_TIME + wither.bb().nextInt(2*MOVE_VARIANCE)-MOVE_VARIANCE;

            determineGroundLevel(wither.locX, wither.locZ);

            finalHeight = groundY + wither.bb().nextDouble()*(MAX_VERTICAL - MIN_VERTICAL) + MIN_VERTICAL;
            double diffX = wither.bb().nextDouble()*(MAX_HORIZONTAL - MIN_HORIZONTAL) + MIN_HORIZONTAL;
            double diffZ = wither.bb().nextDouble()*(MAX_HORIZONTAL - MIN_HORIZONTAL) + MIN_HORIZONTAL;

            targetEntity.setPosition(startLocX + diffX, finalHeight, startLocZ + diffZ);
        }
    }

    //Return average ground level about our "zero" level = 20;
    private void determineGroundLevel(double x, double z)
    {
        int counter=0;
        Block block = world.getBlockAt((int) x,20,(int)z);
        while(block.getType() != Material.AIR && counter < 20)
        {
            block = block.getRelative(BlockFace.UP);
            counter++;
        }
        groundY = 20 + counter;
    }

    //Interrupt
    @Override
    public boolean i()
    {
        return true;
    }
}
