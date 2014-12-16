package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.EntityWitherSkull;
import net.minecraft.server.v1_8_R1.MathHelper;

public class PathfinderPentagram extends AbstractWitherMove {

    private final static float SKULL_COUNT = 360;
    private boolean isActive;
    public PathfinderPentagram(CustomWither wither)
    {
        super(wither);
        this.a(2);
        isActive = false;
    }

    @Override
    public boolean a()
    {

        if (wither.bb().nextInt(100) != 0)
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
    }

    @Override
    public void d()
    {
        isActive = false;
    }

    @Override
    public void e()
    {
        for (int i = 0; i < SKULL_COUNT; i++)
        {
            float distanceFromWither = 4;
            float angle = (float) (i * (2 * 3.141592653589793D) / SKULL_COUNT);
            float diffZ = MathHelper.sin(angle) * distanceFromWither;
            float diffX = MathHelper.cos(angle) * distanceFromWither;


            double x = wither.locX;
            double y = wither.locY;
            double z = wither.locZ;

            //EntityWitherSkull entitywitherskull = new EntityWitherSkull(entity.world, entity.locX, entity.locY, entity.locZ, i,0,j);
            EntityWitherSkull entitywitherskull = new EntityWitherSkull(wither.world);
            entitywitherskull.shooter = wither;

            entitywitherskull.setPositionRotation(x + diffX, y + 3, z + diffZ, wither.yaw, wither.pitch);
            entitywitherskull.setPosition(x + diffX, y + 3, z + diffZ);

            entitywitherskull.motX = entitywitherskull.motY = entitywitherskull.motZ = 0.0D;
            double d3 = (double) MathHelper.sqrt(diffX * diffX + 0 + diffZ * diffZ);


            entitywitherskull.dirX = diffX / d3 *0.1D;
            entitywitherskull.dirY = 0 / d3 *0.1D;
            entitywitherskull.dirZ = diffZ / d3 *0.1D;


            wither.world.addEntity(entitywitherskull);
        }
        isActive = false;

    }


}
