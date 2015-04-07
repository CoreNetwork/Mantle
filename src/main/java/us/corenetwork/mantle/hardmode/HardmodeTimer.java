package us.corenetwork.mantle.hardmode;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftWither;
import org.bukkit.entity.Wither;
import org.bukkit.metadata.MetadataValue;
import us.corenetwork.mantle.MantlePlugin;


public class HardmodeTimer implements Runnable {

	@Override
	public void run() {
		handleWither();
	}

	private void handleWither()
	{
		for (World world : Bukkit.getWorlds())
		{
			for (Wither wither : world.getEntitiesByClass(Wither.class))
			{
				MetadataValue value = null;
				
				for (MetadataValue meta : wither.getMetadata("DespawningTime"))
				{
					if (meta.getOwningPlugin() == MantlePlugin.instance)
					{
						value = meta;
						break;
					}
				}
				
				if (value != null)
				{
					if (wither.getLocation().getY() > HardmodeSettings.WITHER_DESPAWNING_Y.integer() || value.asInt() < System.currentTimeMillis() / 1000)
					{
						((CraftWither) wither).getHandle().die();
					}
				}
			}
		}
	}
}
