package us.corenetwork.mantle.farming;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantlePlugin;
import us.corenetwork.mantle.nanobot.commands.LoadCommand;

import java.lang.reflect.Field;

/**
 * Created by Ginaf on 2015-03-20.
 */
public class NetherwartProtocolListener extends PacketAdapter {
    private NBTTagCompound nbtTag;

    public NetherwartProtocolListener() {
        super(MantlePlugin.instance, ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS);
        loadConfig();
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT)
        {
            addNetherwartNBT(event.getPlayer(), event.getPacket().getItemModifier().read(0));

        }
        else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS)
        {
            int windowId = event.getPacket().getIntegers().read(0);

            if (windowId != 0) //Only change for player inventory - does not work apparently
                return;

            ItemStack[] elements = event.getPacket().getItemArrayModifier().read(0);

            for (int i = 0; i < elements.length; i++)
            {
                if (elements[i] != null)
                {
                    addNetherwartNBT(event.getPlayer(), elements[i]);
                }
            }
        }
    }

    private void addNetherwartNBT(Player player, ItemStack stack)
    {
        if (stack == null)
            return;

        if (stack.getType() != Material.NETHER_STALK)
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
        nbtTag = LoadCommand.load("netherwart");
    }
}

