package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.EntityWitherSkull;
import net.minecraft.server.v1_8_R1.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ginaf on 2014-12-21.
 */
public class MoveMinions extends AbstractWitherMove {

    private int DURATION;
    private int SPAWN_RADIUS_MIN;
    private int SPAWN_RADIUS_MAX;

    private int timeLeft;
    private List<Location> playerLocations;

    public MoveMinions(CustomWither wither)
    {
        super(wither, "Minions", "Mi");
        this.a(2);
    }

    @Override
    protected void initializeMoveConfig()
    {
        DURATION = HardmodeSettings.WITHER_PH_MI_DURATION.integer();
        SPAWN_RADIUS_MIN = HardmodeSettings.WITHER_PH_MI_MINION_SPAWN_RADIUS_MIN.integer();
        SPAWN_RADIUS_MAX = HardmodeSettings.WITHER_PH_MI_MINION_SPAWN_RADIUS_MAX.integer();


        MANA_COST = HardmodeSettings.WITHER_PH_MI_MANACOST.integer();
        COOLDOWN = HardmodeSettings.WITHER_PH_MI_COOLDOWN.integer();
        NORMAL_ATTACK = HardmodeSettings.WITHER_PH_MI_NORMALATTACK.bool();
    }

    @Override
    public void c()
    {
        super.c();
        timeLeft = DURATION;

        spawnMinions();
    }

    @Override
    public void e()
    {



        timeLeft--;
        isActive = timeLeft != 0;
    }

    private void spawnMinions()
    {
        playerLocations = new ArrayList<Location>();
        for(Object o : wither.getTargetList())
        {
            EntityLiving entityLiving = (EntityLiving) o;
            Location loc = new Location(wither.bukkitWorld, entityLiving.locX, entityLiving.locY, entityLiving.locZ);
            playerLocations.add(loc);
        }

        float angleLong, offR, newX, newZ;

        for(Location loc : playerLocations)
        {
            boolean found = false;
            int tries = 0;
            Block block = null;

            while(!found && tries < 10)
            {
                angleLong = wither.bb().nextFloat() * 6.28318530718F;
                offR = wither.bb().nextFloat() * (SPAWN_RADIUS_MAX - SPAWN_RADIUS_MIN) + SPAWN_RADIUS_MIN;
                newX = loc.getBlockX() + offR * MathHelper.cos(angleLong);
                newZ = loc.getBlockZ() + offR * MathHelper.sin(angleLong);

                block = wither.bukkitWorld.getBlockAt((int) newX, (int) loc.getY(), (int) newZ);

                int airLength = 0;

                for (int i = 0; i < 20; i++)
                {
                    if (block.getType() == Material.AIR)
                    {
                        airLength++;
                    }
                    else
                    {
                        airLength = 0;
                    }

                    block = block.getRelative(BlockFace.UP);

                    if (airLength == 7)
                    {
                        found = true;
                        break;
                    }
                }
                tries++;
            }


            if(found)
            {
                Location shotLoc = block.getLocation();


                CustomWitherSkull entitywitherskull = new CustomWitherSkull(wither.world);
                entitywitherskull.shouldSpawnMinions = true;
                entitywitherskull.shooter = wither;

                entitywitherskull.setPositionRotation(shotLoc.getX(), shotLoc.getY() , shotLoc.getZ(), 10, 270);

                entitywitherskull.motX = entitywitherskull.motY = entitywitherskull.motZ = 0.0D;

                entitywitherskull.dirX = 0;
                entitywitherskull.dirY = -0.1D;
                entitywitherskull.dirZ = 0;

                wither.world.addEntity(entitywitherskull);
            }

        }
    }


}

