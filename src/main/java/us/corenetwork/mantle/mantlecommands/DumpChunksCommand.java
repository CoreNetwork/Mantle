package us.corenetwork.mantle.mantlecommands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.server.v1_8_R1.ChunkProviderServer;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.PlayerChunkMap;
import net.minecraft.server.v1_8_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;


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

        dumpChunks(world);

		Util.Message("Chunks dumped.", sender);
	}

    public static void dumpChunks(CraftWorld world)
    {
        File file = new File(MantlePlugin.instance.getDataFolder(), "chunkdata.txt");


        WorldServer nmsWorld = world.getHandle();
        PlayerChunkMap map = nmsWorld.getPlayerChunkMap();

        try
        {
            Method methodGetPlayerChunk = map.getClass().getDeclaredMethod("a", int.class, int.class, boolean.class);
            methodGetPlayerChunk.setAccessible(true);

            Class playerChunkClass = Class.forName("net.minecraft.server.v1_8_R1.PlayerChunk");
            Field fieldPlayerList = playerChunkClass.getDeclaredField("b");
            fieldPlayerList.setAccessible(true);

            ChunkProviderServer cps = nmsWorld.chunkProviderServer;

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (Chunk chunk : world.getLoadedChunks())
            {
                writer.write(Integer.toString(chunk.getX()));
                writer.write(" ");
                writer.write(Integer.toString(chunk.getZ()));
                writer.write(" ");

                Object pChunk = methodGetPlayerChunk.invoke(map, chunk.getX(), chunk.getZ(), false);

                writer.write(" ");
                writer.write("[ ");
                writer.write("InUse: " + world.isChunkInUse(chunk.getX(), chunk.getZ()));
                writer.write(",Unloading: " + cps.unloadQueue.contains(chunk.getX(), chunk.getZ()) + ", ");

                if (pChunk != null)
                {
                    List<EntityPlayer> playerList = (List<EntityPlayer>) fieldPlayerList.get(pChunk);
                    for (EntityPlayer playerInChunk : playerList)
                    {
                        writer.write(playerInChunk.getName());
                        writer.write(", ");
                    }
                }

                writer.write("]");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }
}
