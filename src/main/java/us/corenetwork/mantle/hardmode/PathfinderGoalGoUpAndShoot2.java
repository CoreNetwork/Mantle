package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.RandomPositionGenerator;
import net.minecraft.server.v1_8_R1.Vec3D;


public class PathfinderGoalGoUpAndShoot2 extends AbstractWitherMove {

    private double b;
    private double c;
    private double d;
    private double e;
    private int f;
    private boolean g;

    public PathfinderGoalGoUpAndShoot2(CustomWither wither, double d0)
    {
        this(wither, d0, 120);
    }

    public PathfinderGoalGoUpAndShoot2(CustomWither wither, double d0, int i)
    {
        super(wither);
        this.e = d0;
        this.f = i;
        this.a(1);
        this.g = true;
    }

    public boolean a()
    {
        if (this.g == false)
            return false;

        Vec3D vec3d = RandomPositionGenerator.a(this.wither, 10, 7);

        if (vec3d == null)
        {
            return false;
        } else
        {
            this.b = vec3d.a;
            this.c = vec3d.b;
            this.d = vec3d.c;
            this.g = false;
            return true;
        }
    }

    public boolean b()
    {
        return !this.wither.getNavigation().m();
    }

    public void c()
    {
        this.wither.getNavigation().a(this.b, this.c, this.d, this.e);
    }

    public void f()
    {
        this.g = true;
    }

    public void b(int i)
    {
        this.f = i;
    }
}

