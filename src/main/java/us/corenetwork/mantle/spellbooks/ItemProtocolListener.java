package us.corenetwork.mantle.spellbooks;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import us.corenetwork.mantle.MantlePlugin;


public class ItemProtocolListener extends PacketAdapter {

	public ItemProtocolListener() {
		super(MantlePlugin.instance, ListenerPriority.NORMAL, Play.Server.SET_SLOT, Play.Server.WINDOW_ITEMS);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacketType() == Play.Server.SET_SLOT)
		{
			tryAddFakeEnchantment(event.getPacket().getItemModifier().read(0));

		}
		else if (event.getPacketType() == Play.Server.WINDOW_ITEMS)
		{
			ItemStack[] elements = event.getPacket().getItemArrayModifier().read(0);

			for (int i = 0; i < elements.length; i++) {
				if (elements[i] != null) {
					tryAddFakeEnchantment(elements[i]);
				}
			}
		}	
	}
	
	private static void tryAddFakeEnchantment(ItemStack stack)
	{
		if (stack == null || SpellbookItem.parseSpellbook(stack) == null)
			return;

        stack.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 10);


//		try
//		{
//			Field handleField = CraftItemStack.class.getDeclaredField("handle");
//			handleField.setAccessible(true);
//
//			net.minecraft.server.v1_7_R4.ItemStack nmsStack = (net.minecraft.server.v1_7_R4.ItemStack) handleField.get(stack);
//			if (nmsStack.tag == null)
//			{
//				nmsStack.tag = new NBTTagCompound();
//			}
//
//			if (nmsStack.tag.get("ench") == null)
//			{
//				nmsStack.tag.set("ench", new NBTTagList());
//			}
//
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
	}
	
	public static void init()
	{
		ItemProtocolListener listener = new ItemProtocolListener();
		ProtocolLibrary.getProtocolManager().addPacketListener(listener);

	}





}
