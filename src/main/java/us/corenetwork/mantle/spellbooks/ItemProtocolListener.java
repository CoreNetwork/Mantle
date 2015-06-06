package us.corenetwork.mantle.spellbooks;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.core_network.cornel.common.Messages;
import us.corenetwork.mantle.MantlePlugin;


public class ItemProtocolListener extends PacketAdapter {

    public ItemProtocolListener() {
        super(MantlePlugin.instance, ListenerPriority.NORMAL, Play.Server.SET_SLOT, Play.Server.WINDOW_ITEMS);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == Play.Server.SET_SLOT)
        {
            tryAddFakeEnchantment(event.getPlayer(), event.getPacket().getItemModifier().read(0));

        }
        else if (event.getPacketType() == Play.Server.WINDOW_ITEMS)
        {
            ItemStack[] elements = event.getPacket().getItemArrayModifier().read(0);

            for (int i = 0; i < elements.length; i++) {
                if (elements[i] != null) {
                    tryAddFakeEnchantment(event.getPlayer(), elements[i]);
                }
            }
        }
    }

    private static void tryAddFakeEnchantment(Player player, ItemStack stack)
    {
        if (stack == null)
            return;

        SpellbookItem book = SpellbookItem.parseSpellbook(stack);
        if (book == null)
            return;

        try
        {
            Field handleField = CraftItemStack.class.getDeclaredField("handle");
            handleField.setAccessible(true);

            //Add more user friendly expire message
            if (book.getExpiringTime() != -1 && player.getGameMode() != GameMode.CREATIVE)
            {
                ItemMeta meta = stack.getItemMeta();
                List<String> lore = meta.getLore();

                String newLore;

                int now = (int) (System.currentTimeMillis() / 1000);
                int hoursLeft = (int) Math.floor((book.getExpiringTime() - now) / 3600.0);

                if (hoursLeft <= 0)
                {
                    newLore = SpellbooksSettings.LORE_BOOK_EXPIRED.string();
                } else if (hoursLeft <= 24) {
                    newLore = SpellbooksSettings.LORE_BOOK_LESS_THAN_DAY.string();
                } else
                {
                    newLore = SpellbooksSettings.LORE_BOOK_DAYS_LEFT.string();

                    int daysLeft = (int) Math.round(hoursLeft / 24.0);

                    newLore = newLore.replace("<Days>", Integer.toString(daysLeft));

                    if (daysLeft == 1)
                        newLore = newLore.replace("<PluralS>", "");
                    else
                        newLore = newLore.replace("<PluralS>", "s");
                }

                newLore = Messages.applyFormattingCodes(newLore);

                lore.set(book.getExpiringTimeLoreLineId(), newLore);
                meta.setLore(lore);
                stack.setItemMeta(meta);
            }

            //Add blank enchantment tag
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = (net.minecraft.server.v1_8_R3.ItemStack) handleField.get(stack);
            if (!nmsStack.hasTag())
            {
                nmsStack.setTag(new NBTTagCompound());
            }

            if (nmsStack.getTag().get("ench") == null)
            {
                nmsStack.getTag().set("ench", new NBTTagList());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void init()
    {
        ItemProtocolListener listener = new ItemProtocolListener();
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);

    }





}
