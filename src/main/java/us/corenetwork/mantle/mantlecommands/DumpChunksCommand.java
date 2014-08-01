package us.corenetwork.mantle.mantlecommands;

import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.PlayerChunkMap;
import net.minecraft.server.v1_7_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;


public class DumpChunksCommand extends BaseMantleCommand {	
	public DumpChunksCommand()
	{
		permission = "dumpchunks";
		desc = "Dump chunk data";
		needPlayer = false;
	}


	public void run(final CommandSender sender, String[] args) {
        if (args.length < 1)
        {
            Util.Message("Usage: /mantle dumpchunks [world]", sender);
            return;
        }

        CraftWorld world = (CraftWorld) Bukkit.getWorld(args[0]);
        if (world == null)
        {
            Util.Message("Invalid world!", sender);
            return;
        }

		Util.Message("Dumping chunk debug data...", sender);

		File file = new File(MantlePlugin.instance.getDataFolder(), "chunkdata.txt");


		WorldServer nmsWorld = world.getHandle();
		PlayerChunkMap map = nmsWorld.getPlayerChunkMap();

		try
		{
			Method methodGetPlayerChunk = map.getClass().getDeclaredMethod("a", int.class, int.class, boolean.class);
			methodGetPlayerChunk.setAccessible(true);

			Class playerChunkClass = Class.forName("net.minecraft.server.v1_7_R3.PlayerChunk");
			Field fieldPlayerList = playerChunkClass.getDeclaredField("b");
			fieldPlayerList.setAccessible(true);
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Chunk chunk : world.getLoadedChunks())
			{
				writer.write(Integer.toString(chunk.getX()));
				writer.write(" ");
				writer.write(Integer.toString(chunk.getZ()));
				writer.write(" ");

				Object pChunk = methodGetPlayerChunk.invoke(map, chunk.getX(), chunk.getZ(), false);
				if (pChunk == null)
					writer.write("0");
				else
				{
					List<EntityPlayer> playerList = (List<EntityPlayer>) fieldPlayerList.get(pChunk);
					writer.write(Integer.toString(playerList.size()));
					writer.write(" ");
					writer.write("[ ");
					for (EntityPlayer playerInChunk : playerList)
					{
						writer.write(playerInChunk.getName());
						writer.write(", ");
					}
					writer.write("]");

				}
				writer.newLine();
			}
			writer.close();
		}
		catch (Exception e)
		{
			Util.Message("Something went wrong!", sender);
			e.printStackTrace();
			return;
		}


		Util.Message("Chunks dumped.", sender);
	}	
}
