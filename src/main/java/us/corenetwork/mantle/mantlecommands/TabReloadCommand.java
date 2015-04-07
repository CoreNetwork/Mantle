package us.corenetwork.mantle.mantlecommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.perks.PerksListener;

import java.security.Permissions;

/**
 * Created by Ginaf on 2015-03-21.
 */
public class TabReloadCommand extends BaseMantleCommand {
    public TabReloadCommand()
    {
        permission = "tabreload";
        desc = "Reload Tab list";
        needPlayer = false;
    }


    public void run(final CommandSender sender, String[] args)
    {
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
        {
            String groupName = MantlePlugin.chat.getPrimaryGroup(onlinePlayer);
            String prefix = MantlePlugin.chat.getGroupPrefix((String) null, groupName);

            String playerlistName = Messages.applyFormattingCodes(prefix + onlinePlayer.getName());

            if (playerlistName.length() > 16)
                playerlistName = playerlistName.substring(0, 16);

            //Bukkit won't update name with colors if name is the same as previous, so we need to add dummy name just to change it
            onlinePlayer.setPlayerListName("dummy");
            onlinePlayer.setPlayerListName(playerlistName);


            PerksListener.applyColoredNameplate(onlinePlayer);
        }
    }
}
