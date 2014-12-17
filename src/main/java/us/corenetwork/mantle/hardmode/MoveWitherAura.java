package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.EntityWitherSkull;
import net.minecraft.server.v1_8_R1.MathHelper;

public class MoveWitherAura extends AbstractWitherMove {

    private int DISTANCE_FROM_WITHER;
    private int SEGMENTS_PER_TICK;
    private int SKULLS_PER_SEGMENT;
    private int DELAY_BETWEEN;
    private int NUM_OF_SHOTS;
    private int CIRCLE_SEGMENTS;
    private int MAX_ANGLE_FORWARD;
    private int MAX_ANGLE_BACKWARDS;

    private float basicDiffMultiplierForward;
    private float basicDiffMultiplierBackwards;

    private float forwardDist;
    private float backwardsDist;

    private boolean isActive;

    //to actually count if should turn off
    private int shotNumber;

    public MoveWitherAura(CustomWither wither)
    {
        super(wither);
        this.a(2);
        isActive = false;
    }

    @Override
    protected void initializeMoveConfig()
    {
        DISTANCE_FROM_WITHER = HardmodeSettings.WITHER_PH_WA_DISTANCE_FROM_WITHER.integer();
        SEGMENTS_PER_TICK = HardmodeSettings.WITHER_PH_WA_SEGMENTS_PER_TICK.integer();
        SKULLS_PER_SEGMENT = HardmodeSettings.WITHER_PH_WA_SKULLS_PER_SEGMENT.integer();
        DELAY_BETWEEN = HardmodeSettings.WITHER_PH_WA_DELAY_BETWEEN.integer();
        NUM_OF_SHOTS = HardmodeSettings.WITHER_PH_WA_NUM_OF_SHOTS.integer();
        CIRCLE_SEGMENTS = HardmodeSettings.WITHER_PH_WA_CIRCLE_SEGMENTS.integer();
        MAX_ANGLE_FORWARD = HardmodeSettings.WITHER_PH_WA_MAX_ANGLE_FORWARD.integer();
        MAX_ANGLE_BACKWARDS = HardmodeSettings.WITHER_PH_WA_MAX_ANGLE_BACKWARDS.integer();

        basicDiffMultiplierForward = DISTANCE_FROM_WITHER / MathHelper.cos(MAX_ANGLE_FORWARD / 360);
        basicDiffMultiplierBackwards = DISTANCE_FROM_WITHER / MathHelper.cos(MAX_ANGLE_BACKWARDS / 360);

        forwardDist = DISTANCE_FROM_WITHER * MathHelper.sin(MAX_ANGLE_FORWARD / 360F) / MathHelper.cos(MAX_ANGLE_FORWARD / 360F);
        backwardsDist = DISTANCE_FROM_WITHER * MathHelper.sin(MAX_ANGLE_BACKWARDS / 360F) / MathHelper.cos(MAX_ANGLE_BACKWARDS / 360F);
    }

    @Override
    public boolean a()
    {

        if (wither.ticksLived % 100 != 0)
        {
            return false;
        }
        return true;
    }


    @Override
    public boolean b()
    {
        return isActive;
    }

    @Override
    public void c()
    {
        isActive = true;
        shotNumber = 1;
    }

    @Override
    public void d()
    {
        isActive = false;
    }

    @Override
    public void e()
    {
        if(shotNumber > NUM_OF_SHOTS)
        {
            isActive = false;
        }
        else
        {
            for (int i = 0; i < SEGMENTS_PER_TICK; i++)
            {
                int randomSegment = wither.bb().nextInt(CIRCLE_SEGMENTS);
                float angle = (float) (randomSegment * 6.28318530718 / CIRCLE_SEGMENTS);
                float diffZ = MathHelper.sin(angle) * DISTANCE_FROM_WITHER;
                float diffX = MathHelper.cos(angle) * DISTANCE_FROM_WITHER;

                float maxDiffZ = diffZ * basicDiffMultiplierForward;
                float maxDiffX = diffX * basicDiffMultiplierForward;

                float minDiffZ = diffZ * basicDiffMultiplierBackwards;
                float minDiffX = diffX * basicDiffMultiplierBackwards;

                float randomDist = wither.bb().nextFloat() * (forwardDist + backwardsDist) - backwardsDist;

                if(randomDist > 0)
                {
                    diffZ = maxDiffZ * randomDist/forwardDist;
                    diffX = maxDiffX * randomDist/forwardDist;
                }
                else
                {
                    diffZ = -minDiffZ * randomDist/backwardsDist;
                    diffX = -minDiffX * randomDist/backwardsDist;
                }


                double x = wither.locX;
                double y = wither.locY;
                double z = wither.locZ;

                //EntityWitherSkull entitywitherskull = new EntityWitherSkull(entity.world, entity.locX, entity.locY, entity.locZ, i,0,j);
                EntityWitherSkull entitywitherskull = new EntityWitherSkull(wither.world);
                entitywitherskull.shooter = wither;

                entitywitherskull.setPositionRotation(x + diffX, y + 3, z + diffZ, wither.yaw, wither.pitch);
                entitywitherskull.setPosition(x + diffX, y + 3, z + diffZ);

                entitywitherskull.motX = entitywitherskull.motY = entitywitherskull.motZ = 0.0D;

                double d3 = (double) MathHelper.sqrt(diffX * diffX + DISTANCE_FROM_WITHER*DISTANCE_FROM_WITHER + diffZ * diffZ);


                entitywitherskull.dirX = diffX / d3 *0.1D;
                entitywitherskull.dirY = -DISTANCE_FROM_WITHER / d3 *0.1D;
                entitywitherskull.dirZ = diffZ / d3 *0.1D;


                wither.world.addEntity(entitywitherskull);
            }
        }


    }


}
