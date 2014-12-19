package us.corenetwork.mantle.restockablechests;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.reflect.Field;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;


public class CompassProtocolListener extends PacketAdapter {
    private NBTTagCompound nbtTag;

    public CompassProtocolListener() {
        super(MantlePlugin.instance, ListenerPriority.NORMAL, Play.Server.SET_SLOT, Play.Server.WINDOW_ITEMS);
        loadConfig();
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == Play.Server.SET_SLOT)
        {
            addCompassNBT(event.getPlayer(), event.getPacket().getItemModifier().read(0));

        }
        else if (event.getPacketType() == Play.Server.WINDOW_ITEMS)
        {
            int windowId = event.getPacket().getIntegers().read(0);

            if (windowId != 0) //Only change for player inventory - does not work apparently
                return;

            ItemStack[] elements = event.getPacket().getItemArrayModifier().read(0);

            for (int i = 0; i < elements.length; i++)
            {
                if (elements[i] != null)
                {
                    addCompassNBT(event.getPlayer(), elements[i]);
                }
            }
        }
    }

    private void addCompassNBT(Player player, ItemStack stack)
    {
        if (stack == null)
            return;

        if (stack.getType() != Material.COMPASS)
            return;

        try
        {
            Field handleField = CraftItemStack.class.getDeclaredField("handle");
            handleField.setAccessible(true);


            //Add blank enchantment tag
            net.minecraft.server.v1_8_R1.ItemStack nmsStack = (net.minecraft.server.v1_8_R1.ItemStack) handleField.get(stack);
            nmsStack.setTag(nbtTag);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadConfig()
    {
        nbtTag = LoadCommand.load(RChestSettings.COMPASS_NBT_TAG.string());
    }




}
