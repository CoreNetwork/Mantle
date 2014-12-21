package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ginaf on 2014-12-10.
 */
public class MoveStomp extends AbstractWitherMove {

    private int GROUND_TIME;
    private int MIN_FOLLOWING_TIME;
    private int MAX_FOLLOWING_TIME;
    private double STOMP_MAX_DISTANCE;
    private double DRILL_MAX_DISTANCE_FLAT;

    private StompPhase phase;



    private int onGroundTime;
    private boolean touchedGround;
    private int followingTime;

    private EntityLiving target;
    private List<EntityLiving> alreadyStomped;

    public MoveStomp(CustomWither wither)
    {
        super(wither, "Stomp", "St");
        this.a(3);
        alreadyStomped = new ArrayList<EntityLiving>();
    }

    @Override
    protected void initializeMoveConfig()
    {
        GROUND_TIME = HardmodeSettings.WITHER_PH_ST_GROUND_TIME.integer();
        MIN_FOLLOWING_TIME = HardmodeSettings.WITHER_PH_ST_MIN_FOLLOWING_TIME.integer();
        MAX_FOLLOWING_TIME = HardmodeSettings.WITHER_PH_ST_MAX_FOLLOWING_TIME.integer();
        STOMP_MAX_DISTANCE = HardmodeSettings.WITHER_PH_ST_STOMP_MAX_DISTANCE.doubleNumber();
        DRILL_MAX_DISTANCE_FLAT = HardmodeSettings.WITHER_PH_ST_DRILL_MAX_DISTANCE_FLAT.doubleNumber();

        MANA_COST = HardmodeSettings.WITHER_PH_ST_MANACOST.integer();
        COOLDOWN = HardmodeSettings.WITHER_PH_ST_COOLDOWN.integer();
        NORMAL_ATTACK = HardmodeSettings.WITHER_PH_ST_NORMALATTACK.bool();
    }

    @Override
    public void c()
    {
        super.c();
        onGroundTime = 0;
        touchedGround = false;
        alreadyStomped.clear();
        phase = StompPhase.FINISHED;

    }

    @Override
    public void e()
    {
        switch (phase)
        {
            case FINISHED:
                target = pickTarget();
                //No target found, end stomp move
                if(target == null)
                {
                    isActive = false;
                    return;
                }
                phase = StompPhase.FOLLOWING;
                followingTime = 0;
                wither.setGoalTarget(target, EntityTargetEvent.TargetReason.CUSTOM, false);
                break;
            case STOMPING:

                if (wither.onGround || wither.inWater)
                {
                    touchedGround = true;
                    wither.motY = 0;
                    wither.setSuffCounter(1);
                    wither.setInvulnerable(false);
                }

                if(touchedGround)
                {
                    onGroundTime++;
                    if (onGroundTime == GROUND_TIME)
                    {
                        phase = StompPhase.FINISHED;
                        onGroundTime = 0;
                    }
                }
                else
                {
                    wither.motY *= 4;
                }

                break;
            case DRILLING:
                //one tick something, probably, maybe with delay
                break;
            case FOLLOWING:

                if(followingTime >= MIN_FOLLOWING_TIME)
                {
                    if (shouldStomp())
                    {
                        phase = StompPhase.STOMPING;
                        touchedGround = false;
                        wither.setGoalTarget(null);
                        wither.motY = -0.1;
                        wither.setInvulnerable(true);
                        return;
                    } else if (shouldDrill())
                    {
                        phase = StompPhase.DRILLING;
                        return;
                    }
                }
                followingTime++;
                break;
        }
    }

    public EntityLiving pickTarget()
    {
        for(Object o : wither.getTargetList())
        {
            EntityLiving entityLiving = (EntityLiving) o;
            if(!alreadyStomped.contains(entityLiving))
            {
                alreadyStomped.add(entityLiving);
                return entityLiving;
            }
        }
        return null;
    }

    public boolean shouldStomp()
    {
        if(followingTime >= MAX_FOLLOWING_TIME)
            return true;

        double groundUnderWither = groundYUnderWither();
        double distanceSq = (wither.locX - target.locX)*(wither.locX - target.locX) + (wither.locZ - target.locZ)*(wither.locZ - target.locZ) + (groundUnderWither - target.locY)*(groundUnderWither - target.locY);

        return distanceSq < STOMP_MAX_DISTANCE*STOMP_MAX_DISTANCE;
    }

    public boolean shouldDrill()
    {
        if(followingTime >= MAX_FOLLOWING_TIME)
            return true;

        double groundUnderWither = groundYUnderWither();
        double distanceSqFFlat = (wither.locX - target.locX)*(wither.locX - target.locX) + (wither.locZ - target.locZ)*(wither.locZ - target.locZ);

        return distanceSqFFlat < DRILL_MAX_DISTANCE_FLAT && target.locY + 2 < groundUnderWither;
    }

    private double groundYUnderWither()
    {
        double y = 0;
        Block block = wither.bukkitWorld.getBlockAt((int) wither.locX, (int) wither.locY, (int) wither.locZ);
        while(block.getType() == Material.AIR)
        {
            block = block.getRelative(BlockFace.DOWN);
            y++;
        }

        return wither.locY - y;
    }



}

enum StompPhase
{
    FINISHED,
    FOLLOWING,
    STOMPING,
    DRILLING

}
