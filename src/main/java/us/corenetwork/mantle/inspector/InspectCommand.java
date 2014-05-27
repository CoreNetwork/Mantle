package us.corenetwork.mantle.inspector;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;



public class InspectCommand {	
	public static void command(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("You must be player!");
			return;
		}
		
		if (!sender.hasPermission("mantle.inspector.command"))
		{
			Util.Message(Settings.getString(Setting.MESSAGE_NO_PERMISSION), sender);
			return;
		}
		
		if (args.length < 1)
		{
			Util.Message(InspectorSettings.MESSAGE_COMMAND_SYNTAX.string(), sender);
			return;
		}
		
		String action = args[0];
		Player player = (Player) sender;
		
		if (action.equals("start"))
		{
			start(player);
			return;
		}
		
		if (!InspectorSession.sessions.containsKey(player.getUniqueId()))
		{
			Util.Message(InspectorSettings.MESSAGE_SESSION_NOT_ACTIVE.string(), sender);
			return;
		}
		
		InspectorSession session = InspectorSession.sessions.get(player.getUniqueId());
		
		if (action.equals("stop"))
		{
			stop(player);
			return;
		}

		
		if (action.equals("approve"))
		{
			approve(player, session);
			return;
		}
		
		if (action.equals("skip"))
		{
			skip(player, session);
			return;
		}
		
		if (action.equals("reject"))
		{
			reject(player, session);
			return;
		}
		
		if (action.equals("postpone"))
		{
			postpone(player, session);
			return;
		}
		
		Util.Message(InspectorSettings.MESSAGE_COMMAND_SYNTAX.string(), sender);
	}
	
	private static void start(Player player)
	{		
		if (InspectorSession.sessions.containsKey(player.getUniqueId()))
		{
			Util.Message(InspectorSettings.MESSAGE_SESSION_ALREADY_ACTIVE.string(), player);
			return;
		}
		
		InspectorSession session = new InspectorSession();
		InspectorSession.sessions.put(player.getUniqueId(), session);
		
		session.teleportToNext(player);
	}
	
	private static void stop(Player player)
	{
		Util.Message(InspectorSettings.MESSAGE_STOP.string(), player);
		InspectorSession.sessions.remove(player.getUniqueId());
	}
	
	private static void approve(Player player, InspectorSession session)
	{
		Util.Message(InspectorSettings.MESSAGE_APPROVE.string(), player);
		session.setState(1);
		session.teleportToNext(player);
	}
	
	private static void reject(Player player, InspectorSession session)
	{
		Util.Message(InspectorSettings.MESSAGE_REJECT.string(), player);
		session.rejectStructure();
		session.teleportToNext(player);
	}
	
	private static void skip(Player player, InspectorSession session)
	{
		Util.Message(InspectorSettings.MESSAGE_SKIP.string(), player);
		session.skip();
		session.teleportToNext(player);
	}
	
	private static void postpone(Player player, InspectorSession session)
	{
		Util.Message(InspectorSettings.MESSAGE_POSTPONE.string(), player);
		session.setState(2);
		session.teleportToNext(player);
	}
	
	
}
