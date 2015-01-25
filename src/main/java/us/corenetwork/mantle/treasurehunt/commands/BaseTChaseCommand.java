package us.corenetwork.mantle.treasurehunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Setting;
import us.corenetwork.mantle.Settings;
import us.corenetwork.mantle.Util;

/**
 * Created by Ginaf on 2015-01-25.
 */
public abstract class BaseTChaseCommand {
    public Boolean needPlayer;
    public String desc;
    public String permission;

    public abstract void run(CommandSender sender, String[] args);

    public Boolean execute(CommandSender sender, String[] args)
    {
        if (args.length > 0)
        {
            String[] newargs = new String[args.length - 1];
            for (int i = 1; i < args.length; i++)
            {
                newargs[i - 1] = args[i];
            }
            args = newargs;
        }

        if (!(sender instanceof Player) && needPlayer)
        {
            Util.Message("Sorry, but you need to execute this command as player.", sender);
            return true;
        }
        if (sender instanceof Player && !Util.hasPermission(sender,"mantle.treasurechase.command." + permission))
        {
            Util.Message(Settings.getString(Setting.MESSAGE_NO_PERMISSION), sender);
            return true;
        }

        run(sender, args);
        return true;
    }
}
