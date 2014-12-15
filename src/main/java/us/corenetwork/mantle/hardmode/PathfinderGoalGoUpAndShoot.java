package us.corenetwork.mantle.hardmode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import us.corenetwork.mantle.MantlePlugin;
import net.minecraft.server.v1_8_R1.EntityWither;
import net.minecraft.server.v1_8_R1.EntityWitherSkull;
import net.minecraft.server.v1_8_R1.PathfinderGoal;

public class PathfinderGoalGoUpAndShoot extends AbstractPathfinderGoal {

	private CustomWither wither;
	private boolean running;
	private int countdown;


	public PathfinderGoalGoUpAndShoot(CustomWither wither)
	{
		this.wither = wither;
		this.a(1);
	}




	@Override
	public boolean a()
	{
		if(wither.bb().nextInt(100) < 2)
		{
			return true;
		}
/*
		if(PathfinderGoalGoUpAndShoot.done == false)
		{
			PathfinderGoalGoUpAndShoot.done = true;
			return true;
		}
		*/
		return false;
	}

	
	@Override
	public boolean b()
	{
		return countdown > 0;
	}
	
	@Override
	public void c()
	{
		running = true;
		countdown = 500;
	}
	
	@Override
	public void d()
	{
		running = false;
	}
	
	@Override
	public void e()
	{
		--countdown;

		if(wither.world.getTime() % 100 != 0)
			return;
		
		for(int i = -5; i<=5;++i)
		{
			for(int j = -5;j<=5;++j)
			{
					EntityWitherSkull entitywitherskull = new EntityWitherSkull(wither.world, wither,i,0,j);
					entitywitherskull.locX = wither.locX;
					entitywitherskull.locY = wither.locY + 3;
					entitywitherskull.locZ = wither.locZ;

					entitywitherskull.setCharged(true);
					wither.world.addEntity(entitywitherskull);
			}
		}
		
		
		
		
	}
	
	@Override
	public boolean i()
	{
		return !running;
	}
}
