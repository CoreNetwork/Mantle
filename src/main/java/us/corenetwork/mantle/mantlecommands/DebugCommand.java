package us.corenetwork.mantle.mantlecommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Team;
import us.corenetwork.core.scoreboard.CoreScoreboardManager;
import us.corenetwork.mantle.MLog;

/**
 * Created by Ginaf on 2015-03-23.
 */
public class DebugCommand extends BaseMantleCommand {
    public DebugCommand()
    {
        permission = "debug";
        desc = "Print some debug";
        needPlayer = false;
    }


    public void run(final CommandSender sender, String[] args)
    {
        String mode = "";
        if(args.length == 1)
        {
            mode = args[0];
        }
        switch (mode)
        {
            default:
                for(Team tt : CoreScoreboardManager.getTeamsScoreboard().getTeams())
                {
                    MLog.forceDebug(tt.getName() + "  " + tt.getPrefix()  + "woot" );
                    for(OfflinePlayer offlinePlayer : tt.getPlayers())
                        MLog.forceDebug(offlinePlayer.getName());
                }
                break;
        }
    }

}
