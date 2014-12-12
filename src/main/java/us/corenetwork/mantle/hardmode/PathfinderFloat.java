package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.EntityWither;
import net.minecraft.server.v1_8_R1.Navigation;

public class PathfinderFloat extends AbstractPathfinderGoal {

    private EntityWither a;

    public PathfinderFloat(EntityWither entityinsentient) {
        this.a = entityinsentient;
        this.a(5);
    }

    public boolean a()
    {
        return true;
    }

    public void e()
    {
        //float R blocks above land
        double R = 10;

        double groundY = a.world.getHighestBlockYAt(new BlockPosition(a.locX, a.locY, a.locZ)).getY();

        if (a.locY < groundY + R / 2 || !a.ck() && a.locY < groundY + R) {
            if (a.motY < 0.0D) {
                a.motY = 0.0D;
            }

            a.motY += (0.5D - a.motY) * 0.6000000238418579D;
        }
    }

    @Override
    public boolean i() {
        return true;
    }
}
