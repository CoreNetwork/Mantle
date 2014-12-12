package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.ControllerMove;
import net.minecraft.server.v1_8_R1.EntityWither;

import java.util.Random;

/**
 * Created by Ginaf on 2014-12-10.
 */
public class PathfinderStomp extends AbstractPathfinderGoal {

    private EntityWither a;


    private boolean stompMove;
    private int onGroundTime = 0;

    public PathfinderStomp(EntityWither entityghast) {
        this.a = entityghast;
        stompMove = false;
        this.a(1);
    }

    public boolean a() {
        return (a.world.getTime() % 300 == 0);
    }

    public boolean b() {
        return stompMove;
    }

    public void c() {
        stompMove = true;
        a.motY = -0.1D;
    }

    @Override
    public void e() {
        a.motY *= 2;

        if(a.onGround)
        {
            onGroundTime++;
            if(onGroundTime == 100)
            {
                stompMove = false;
                onGroundTime = 0;
            }
        }
    }

    @Override
    public void d() {
        stompMove = false;
    }


}
