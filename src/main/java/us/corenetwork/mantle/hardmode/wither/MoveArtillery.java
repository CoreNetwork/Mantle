package us.corenetwork.mantle.hardmode.wither;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

public class MoveArtillery extends AbstractWitherMove {

    private int MIN_VERTICAL;
    private int MAX_VERTICAL;
    private int MAX_HORIZONTAL;
    private int MIN_HORIZONTAL;
    private int MOVE_BASIC_TIME;
    private int MOVE_VARIANCE;


    private double startLocX;
    private double startLocZ;

    private long nextMoveTime;

    double groundY;
    double finalHeight;

    private TargetEntity targetEntity;


    public MoveArtillery(CustomWither wither)
    {
        super(wither, "Artillery", "Ar");
        this.a(1);
    }

    @Override
    protected void initializeMoveConfig()
    {
        MIN_VERTICAL = HardmodeSettings.WITHER_PH_SA_MIN_VERTICAL.integer();
        MAX_VERTICAL = HardmodeSettings.WITHER_PH_SA_MAX_VERTICAL.integer();
        MAX_HORIZONTAL = HardmodeSettings.WITHER_PH_SA_MAX_HORIZONTAL.integer();
        MIN_HORIZONTAL = HardmodeSettings.WITHER_PH_SA_MIN_HORIZONTAL.integer();
        MOVE_BASIC_TIME = HardmodeSettings.WITHER_PH_SA_MOVE_BASIC_TIME.integer();
        MOVE_VARIANCE = HardmodeSettings.WITHER_PH_SA_MOVE_TIME_VARIANCE.integer();

        MANA_COST = HardmodeSettings.WITHER_PH_SA_MANACOST.integer();
        COOLDOWN = HardmodeSettings.WITHER_PH_SA_COOLDOWN.integer();
        NORMAL_ATTACK = HardmodeSettings.WITHER_PH_SA_NORMALATTACK.bool();
    }

    @Override
    public void cleanUp()
    {
        if(targetEntity != null)
            wither.world.removeEntity(targetEntity);
    }


    public boolean a()
    {
        //Not relying on super() here, coz we need the Artillery to happen right away.
        //TODO refactor to something better.
        return !isOnCooldown() && hasEnoughMana() && !wither.isInSpawningPhase();
    }

    //Init phase
    @Override
    public void c()
    {
        super.c();
        determineGroundLevel(wither.locX, wither.locZ);
        finalHeight = groundY + MIN_VERTICAL;

        targetEntity = new TargetEntity(wither.world, wither.locX, wither.locY, wither.locZ);

        wither.world.addEntity(targetEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        wither.setGoalTarget(targetEntity, EntityTargetEvent.TargetReason.CUSTOM, false);

        startLocX = wither.locX;
        startLocZ = wither.locZ;

        nextMoveTime = wither.world.getTime() + MOVE_BASIC_TIME + MantlePlugin.random.nextInt(2 * MOVE_VARIANCE) - MOVE_VARIANCE;

    }


    //Reset phase
    @Override
    public void d()
    {
        wither.setGoalTarget(null);
        cleanUp();
    }

    //Run phase (each tick)
    @Override
    public void e()
    {
        if (wither.world.getTime() > nextMoveTime)
        {
            nextMoveTime = wither.world.getTime() + MOVE_BASIC_TIME + MantlePlugin.random.nextInt(2 * MOVE_VARIANCE) - MOVE_VARIANCE;

            determineGroundLevel(wither.locX, wither.locZ);

            finalHeight = groundY + MantlePlugin.random.nextDouble() * (MAX_VERTICAL - MIN_VERTICAL) + MIN_VERTICAL;
            double diffX = MantlePlugin.random.nextDouble() * (MAX_HORIZONTAL - MIN_HORIZONTAL) + MIN_HORIZONTAL;
            double diffZ = MantlePlugin.random.nextDouble() * (MAX_HORIZONTAL - MIN_HORIZONTAL) + MIN_HORIZONTAL;

            targetEntity.setPosition(startLocX + diffX, finalHeight, startLocZ + diffZ);
        }
    }

    @Override
    public boolean i()
    {
        return true;
    }


    //Return average ground level about our "zero" level = 20;
    private void determineGroundLevel(double x, double z)
    {
        int counter = 0;
        Block block = wither.bukkitWorld.getBlockAt((int) x, 1, (int) z);
        while (block.getType() != Material.AIR && counter < 40)
        {
            block = block.getRelative(BlockFace.UP);
            counter++;
        }
        groundY = counter;
    }
}
