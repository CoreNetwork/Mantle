package us.corenetwork.mantle.mantlecommands;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.Util;


public class ChunkInfoCommand extends BaseMantleCommand {	
	public ChunkInfoCommand()
	{
		permission = "chunkinfo";
		desc = "Display chunk info for current world";
		needPlayer = false;
	}


	public void run(final CommandSender sender, String[] args) {
        if (args.length < 1)
        {
            Messages.send("Usage: /mantle chunkinfo [world]", sender);
            return;
        }

        CraftWorld world = (CraftWorld) Bukkit.getWorld(args[0]);
        if (world == null)
        {
            Messages.send("Invalid world!", sender);
            return;
        }

		try
		{

			int loadedChunks = world.getLoadedChunks().length;
			int chunkGCTickCount = 0;  

			Field tickCountField = CraftWorld.class.getDeclaredField("chunkGCTickCount");
			tickCountField.setAccessible(true);
			chunkGCTickCount = tickCountField.getInt(world);

            Messages.send("Loaded chunks: " + loadedChunks, sender);
            Messages.send("Elapsed Chunk GC tick count: " + chunkGCTickCount, sender);
		}
		catch (Exception e)
		{
            Messages.send("Something went wrong!", sender);
			e.printStackTrace();
			return;
		}


	}	
}
