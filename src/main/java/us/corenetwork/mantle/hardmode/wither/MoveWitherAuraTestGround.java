package us.corenetwork.mantle.hardmode.wither;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.v1_8_R2.MathHelper;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.hardmode.HardmodeSettings;

public class MoveWitherAuraTestGround extends AbstractWitherMove {

    private int DISTANCE_FROM_WITHER;
    private int SEGMENTS_PER_TICK;
    private int SKULLS_PER_SEGMENT;
    private int DELAY_BETWEEN;
    private int NUM_OF_SHOTS;
    private int CIRCLE_SEGMENTS;
    private int MAX_ANGLE_FORWARD;
    private int MAX_ANGLE_BACKWARDS;
    private float DAMAGE_MULTIPLIER;
    private float RADIUS_MULTIPLIER;
    private float forwardDist;
    private float backwardsDist;

    //To not repeat segments in randomization
    private List<Integer> segmentList = new ArrayList<Integer>();
    //to actually count if should turn off
    private int shotNumber;
    private int delay;

    public MoveWitherAuraTestGround(CustomWither wither)
    {
        super(wither, "Wither Aura Test Ground", "WAtg");
        this.a(2);

        for(int i = 0;i<CIRCLE_SEGMENTS;i++)
            segmentList.add(i);
    }

    @Override
    protected void initializeMoveConfig()
    {
        DISTANCE_FROM_WITHER = HardmodeSettings.WITHER_PH_WA_DISTANCE_FROM_WITHER.integer();
        SEGMENTS_PER_TICK = HardmodeSettings.WITHER_PH_WA_SEGMENTS_PER_SHOT.integer();
        SKULLS_PER_SEGMENT = HardmodeSettings.WITHER_PH_WA_SKULLS_PER_SEGMENT.integer();
        DELAY_BETWEEN = HardmodeSettings.WITHER_PH_WA_DELAY_BETWEEN.integer();
        NUM_OF_SHOTS = HardmodeSettings.WITHER_PH_WA_NUM_OF_SHOTS.integer();
        CIRCLE_SEGMENTS = HardmodeSettings.WITHER_PH_WA_CIRCLE_SEGMENTS.integer();
        MAX_ANGLE_FORWARD = HardmodeSettings.WITHER_PH_WA_MAX_ANGLE_FORWARD.integer();
        MAX_ANGLE_BACKWARDS = HardmodeSettings.WITHER_PH_WA_MAX_ANGLE_BACKWARDS.integer();
        DAMAGE_MULTIPLIER = HardmodeSettings.WITHER_PH_WA_DAMAGE_MULTIPLIER.floatNumber();
        RADIUS_MULTIPLIER = HardmodeSettings.WITHER_PH_WA_RADIUS_MULTIPLIER.floatNumber();

        MANA_COST = HardmodeSettings.WITHER_PH_WA_MANACOST.integer();
        COOLDOWN = HardmodeSettings.WITHER_PH_WA_COOLDOWN.integer();
        NORMAL_ATTACK = HardmodeSettings.WITHER_PH_WA_NORMALATTACK.bool();

        forwardDist = DISTANCE_FROM_WITHER * MathHelper.sin(6.28318530718F * MAX_ANGLE_FORWARD / 360F) / MathHelper.cos(6.28318530718F * MAX_ANGLE_FORWARD / 360F);
        backwardsDist = DISTANCE_FROM_WITHER * MathHelper.sin(6.28318530718F * MAX_ANGLE_BACKWARDS / 360F) / MathHelper.cos(6.28318530718F * MAX_ANGLE_BACKWARDS / 360F);
    }

    @Override
    public void c()
    {
        super.c();
        shotNumber = 1;
        delay = 0;
    }

    @Override
    public void e()
    {
        if (shotNumber > NUM_OF_SHOTS)
        {
            isActive = false;
        } else
        {
            if (delay > 0)
            {
                delay--;
                return;
            }

            shotNumber++;
            delay = DELAY_BETWEEN;

            Collections.shuffle(segmentList);

            for (int i = 0; i < SEGMENTS_PER_TICK; i++)
            {
                int randomSegment = segmentList.get(i % CIRCLE_SEGMENTS);
                float angle = (float) (randomSegment * 6.28318530718 / CIRCLE_SEGMENTS);
                float diffZ = MathHelper.sin(angle) * DISTANCE_FROM_WITHER;
                float diffX = MathHelper.cos(angle) * DISTANCE_FROM_WITHER;

                float diffZSpawn = diffZ;
                float diffXSpawn = diffX;

                float maxDiffZ = diffZ / DISTANCE_FROM_WITHER;
                float maxDiffX = diffX / DISTANCE_FROM_WITHER;

                float minDiffZ = diffZ / DISTANCE_FROM_WITHER;
                float minDiffX = diffX / DISTANCE_FROM_WITHER;

                for (int j = 0; j < SKULLS_PER_SEGMENT; j++)
                {
                    float randomDist = MantlePlugin.random.nextFloat() * (forwardDist + backwardsDist) - backwardsDist;

                    if (randomDist > 0)
                    {
                        diffZ = maxDiffZ * randomDist;
                        diffX = maxDiffX * randomDist;
                    } else
                    {
                        diffZ = -minDiffZ * randomDist;
                        diffX = -minDiffX * randomDist;
                    }

                    double x = wither.locX;
                    double y = wither.locY;
                    double z = wither.locZ;

                    CustomWitherSkull entitywitherskull = new CustomWitherSkull(wither.world);
                    entitywitherskull.damage = wither.BASE_DMG * DAMAGE_MULTIPLIER;
                    entitywitherskull.explosionRadius = wither.BS_RADIUS * RADIUS_MULTIPLIER;
                    entitywitherskull.shooter = wither;

                    entitywitherskull.setPositionRotation(x + diffXSpawn, y + 3, z + diffZSpawn, randomSegment, randomSegment);

                    entitywitherskull.motX = entitywitherskull.motY = entitywitherskull.motZ = 0.0D;

                    double d3 = (double) MathHelper.sqrt(diffX * diffX + DISTANCE_FROM_WITHER * DISTANCE_FROM_WITHER + diffZ * diffZ);


                    entitywitherskull.dirX = diffX / d3 * 0.1D;
                    entitywitherskull.dirY = -DISTANCE_FROM_WITHER / d3 * 0.1D;
                    entitywitherskull.dirZ = diffZ / d3 * 0.1D;

                    wither.world.addEntity(entitywitherskull);
                }
            }
        }
    }
}
