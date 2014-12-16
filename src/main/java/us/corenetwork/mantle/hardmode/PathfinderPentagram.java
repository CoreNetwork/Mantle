package us.corenetwork.mantle.hardmode;

import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.EntityWitherSkull;
import net.minecraft.server.v1_8_R1.MathHelper;

public class PathfinderPentagram extends AbstractWitherMove {

	private final static float SKULL_COUNT = 50;
	
	
	private EntityCreature entity;
	private int phase;
	public PathfinderPentagram(EntityCreature entityCreature)
	{
		this.entity = entityCreature;
		this.a(4);
	}
	
	@Override
	public boolean a()
	{
        if (entity.bb().nextInt(100) != 0) {
            return false;
        }
        return true;
	}

	
	@Override
	public boolean b()
	{
		return phase < SKULL_COUNT;
	}
	
	@Override
	public void c()
	{
		phase = 0;
	}
	
	@Override
	public void e()
	{
		for(int i = phase; phase < SKULL_COUNT;phase++)
		{
		float distanceFromWither = 4;
		float angle = (float) (i * (2* 3.141592653589793D)/ SKULL_COUNT);
		float diffZ = MathHelper.sin(angle)* distanceFromWither;
		float diffX = MathHelper.cos(angle)* distanceFromWither;
		
		
		double x = entity.locX;
		double y = entity.locY;
		double z = entity.locZ;
		
		//EntityWitherSkull entitywitherskull = new EntityWitherSkull(entity.world, entity.locX, entity.locY, entity.locZ, i,0,j);
		EntityWitherSkull entitywitherskull = new EntityWitherSkull(entity.world);
		entitywitherskull.shooter = entity;
		
		entitywitherskull.setPositionRotation(x+diffX,y+3,z+diffZ, entity.yaw, entity.pitch);
		entitywitherskull.setPosition(x+diffX,y+3,z+diffZ);
		
		entitywitherskull.motX =entitywitherskull.motY = entitywitherskull.motZ = 0.0D;
		double d3 = (double) MathHelper.sqrt(diffX*diffX + 0 + diffZ*diffZ );
		
		entitywitherskull.dirX = diffX / d3 *0.1D;
		entitywitherskull.dirY = 0 / d3 *0.1D;
		entitywitherskull.dirZ = diffZ / d3 *0.1D;
		
		
		entity.world.addEntity(entitywitherskull);
		}
		//phase++;
		
	}
	
	
	@Override
	public void d()
	{
		phase = 0;
	}
	
}
