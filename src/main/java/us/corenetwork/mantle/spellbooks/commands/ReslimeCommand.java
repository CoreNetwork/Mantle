package us.corenetwork.mantle.spellbooks.commands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.slimespawning.IgnoredSlimeChunks;
import us.corenetwork.mantle.spellbooks.SpellbooksSettings;

public class ReslimeCommand extends BaseMantleCommand {

	public ReslimeCommand()
	{
		permission = "reslime";
		desc = "Remove effect of the unsliming spell from this chunk";
		needPlayer = true;
	}

	
	@Override
	public void run(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		

		
		Chunk chunk = player.getLocation().getBlock().getChunk();		
		if (IgnoredSlimeChunks.isIgnored(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()))
		{
			Util.Message(SpellbooksSettings.MESSAGE_RESLIME_SUCCESS.string(), player);
			IgnoredSlimeChunks.removeChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
		}
		else
		{
			Util.Message(SpellbooksSettings.MESSAGE_RESLIME_FAIL.string(), player);
		}

	}

}
