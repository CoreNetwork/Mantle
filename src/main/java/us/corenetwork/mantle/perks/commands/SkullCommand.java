package us.corenetwork.mantle.perks.commands;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.nanobot.NanobotUtil;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;
import us.corenetwork.mantle.perks.PerksSettings;

/**
 * Created by Ginaf on 2015-01-18.
 */
public class SkullCommand extends BasePerksCommand {

    public SkullCommand()
    {
        permission = "advskulls";
        desc = "Transforms one wither skeleton skull into Player Head";
        needPlayer = true;
    }


    @Override
    public void run(CommandSender sender, String[] args)
    {
        if(args.length < 1)
        {
            Util.Message("Usage: /skull <PlayerName>", sender);
            return;
        }

        Player player = (Player) sender;

        ItemStack itemInHand = player.getItemInHand();

        if(itemInHand.getAmount() != 1 || !(itemInHand.getDurability() == 1 || itemInHand.getDurability() == 3))
        {
            Util.Message(PerksSettings.MESSAGE_SKULL_ONLY_ONE_IN_HAND.string(), sender);
            return;
        }
        CraftItemStack si = (CraftItemStack) itemInHand;

        si.setDurability((short)3);
        SkullMeta skullMeta = (SkullMeta)si.getItemMeta();
        skullMeta.setOwner(args[0]);

        si.setItemMeta(skullMeta);

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = NanobotUtil.getInternalNMSStack(si);
        NBTTagCompound oldTag = nmsStack.getTag();
        NBTTagCompound skullOwnerCompound = oldTag.getCompound("SkullOwner");

        NBTTagCompound tag = LoadCommand.load(PerksSettings.SPECIAL_SKULL_NANOBOT_FILE.string());
        tag.set("SkullOwner", skullOwnerCompound);

        nmsStack.setTag(tag);

        ItemMeta meta = si.getItemMeta();
        if (meta.hasDisplayName())
        {
            meta.setDisplayName(meta.getDisplayName().replace("<OriginalName>", skullMeta.getOwner() + "'s head"));
            si.setItemMeta(meta);
        }
    }
}
