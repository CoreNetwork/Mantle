package us.corenetwork.mantle.mantlecommands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.PlayerChunkMap;
import net.minecraft.server.v1_7_R3.WorldServer;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;


public class ChunkInfoCommand extends BaseMantleCommand {	
	public ChunkInfoCommand()
	{
		permission = "chunkinfo";
		desc = "Display chunk info for current world";
		needPlayer = true;
	}


	public void run(final CommandSender sender, String[] args) {
		Player player = (Player) sender;

		try
		{

			World world = player.getWorld();
			int loadedChunks = world.getLoadedChunks().length;
			int chunkGCTickCount = 0;  

			Field tickCountField = CraftWorld.class.getDeclaredField("chunkGCTickCount");
			tickCountField.setAccessible(true);
			chunkGCTickCount = tickCountField.getInt(world);

			Util.Message("Loaded chunks: " + loadedChunks, sender);
			Util.Message("Elapsed Chunk GC tick count: " + chunkGCTickCount, sender);


			MLog.info(loadedChunks + " " + chunkGCTickCount);

		}
		catch (Exception e)
		{
			Util.Message("Something went wrong!", sender);
			e.printStackTrace();
			return;
		}


	}	
}
