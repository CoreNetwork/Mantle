package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.mantlecommands.BaseMantleCommand;
import us.corenetwork.mantle.slimeballs.SlimeballsModule;
import us.corenetwork.mantle.slimeballs.commands.BaseSlimeballsCommand;
import us.corenetwork.mantle.treasurehunt.THuntModule;
import us.corenetwork.mantle.treasurehunt.THuntSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Ginaf on 2015-01-25.
 */
public class HelpHuntCommand extends BaseTChaseCommand {

    public HelpHuntCommand()
    {
        permission = "help";
        desc = "List all possible commands";
        needPlayer = false;
    }


    public void run(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0 && Util.isInteger(args[0])) page = Integer.parseInt(args[0]);
        List<String> komandes = new ArrayList<String>();

        for (Map.Entry<String, BaseTChaseCommand> e : THuntModule.commands.entrySet())
        {
            if (sender.hasPermission(e.getValue().permission))
                komandes.add(Settings.getCommandDescription(e.getKey(), "chase", e.getValue().desc));
        }
        String[] komande = komandes.toArray(new String[0]);
        Arrays.sort(komande);

        int maxpage = (int) Math.ceil((double) komande.length / (sender instanceof Player ? 15.0 : 30.0));

        if (page > maxpage)
            page = maxpage;

        Util.Message("List of all commands:", sender);
        Util.Message("&8Page " + String.valueOf(page) + " of " + String.valueOf(maxpage), sender);

        for (int i = (page - 1) * 15; i < page * 15; i++)
        {
            if (komande.length < i + 1 || i < 0) break;
            Util.Message(komande[i], sender);
        }
    }
}
