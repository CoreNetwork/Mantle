package us.corenetwork.mantle.mantlecommands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.accessors.Accessors;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.EnumProtocol;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketDataSerializer;
import net.minecraft.server.v1_7_R4.PacketListener;
import net.minecraft.util.com.google.gson.JsonParseException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.corenetwork.mantle.Util;
import us.corenetwork.mantle.armorhologram.HologramPlayerData;


public class TitleCommand extends BaseMantleCommand {
	public TitleCommand()
	{
		permission = "title";
		desc = "Implementation of 1.8 /title command";
		needPlayer = false;

        EnumProtocol.PLAY.b().put(69, TitlePacket.class);

        Map<Class<?>, EnumProtocol> map = (Map<Class<?>, EnumProtocol>)
                Accessors.getFieldAccessor(EnumProtocol.class, Map.class, true).get(EnumProtocol.PLAY);
        map.put(TitlePacket.class, EnumProtocol.PLAY);
	}


	public void run(final CommandSender sender, String[] args) {
        if (args.length < 2)
        {
            Util.Message("Usage: /title <player> <clear/reset/subtitle/times/title> <parameters for specific mode (see wiki)>  ", sender);
            return;
        }

        String playerName = args[0];
        Player player = Bukkit.getPlayerExact(args[0]);
        if (player == null)
        {
            Util.Message("Player needs to be online!", sender);
            return;
        }

        if (!HologramPlayerData.isPlayer18(player))
            return;

        String mode = args[1];
        if (args[1].equals("title"))
            titleCommand(sender, player, args);
        else if (args[1].equals("subtitle"))
            subtitleCommand(sender, player, args);
        else if (args[1].equals("clear"))
            singleCommand(sender, player, args, TitlePacket.ACTION_CLEAR);
        else if (args[1].equals("reset"))
            singleCommand(sender, player, args, TitlePacket.ACTION_RESET);
        else if (args[1].equals("times"))
            timesCommand(sender, player, args);
        else
            Util.Message("Usage: /title <player> <clear/reset/subtitle/times/title> <parameters for specific mode (see wiki)>  ", sender);
    }

    public void titleCommand(CommandSender sender, Player player, String[] args)
    {
        if (args.length < 3)
        {
            Util.Message("Usage: /title <player> title <title> <raw json title>", sender);
            return;
        }

        try
        {
            IChatBaseComponent component = ChatSerializer.a(args[2]);
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            PacketContainer container = new PacketContainer(TitlePacket.packetType, new TitlePacket(TitlePacket.ACTION_TITLE, ChatSerializer.a(component)));

            manager.sendServerPacket(player, container);
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        catch (JsonParseException e)
        {
            sender.sendMessage("Invalid json!");
        }
    }

    public void subtitleCommand(CommandSender sender, Player player, String[] args)
    {
        if (args.length < 3)
        {
            Util.Message("Usage: /title <player> subtitle <subtitle> <raw json title>  ", sender);
            return;
        }

        try
        {
            IChatBaseComponent component = ChatSerializer.a(args[2]);
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            PacketContainer container = new PacketContainer(TitlePacket.packetType, new TitlePacket(TitlePacket.ACTION_SUBTITLE, ChatSerializer.a(component)));

            manager.sendServerPacket(player, container);
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        catch (JsonParseException e)
        {
            sender.sendMessage("Invalid json!");
        }
    }

    public void singleCommand(CommandSender sender, Player player, String[] args, int action)
    {
        try
        {
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            PacketContainer container = new PacketContainer(TitlePacket.packetType, new TitlePacket(action));

            manager.sendServerPacket(player, container);
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public void timesCommand(CommandSender sender, Player player, String[] args)
    {
        if (args.length < 5 || !Util.isInteger(args[2]) || !Util.isInteger(args[3]) || !Util.isInteger(args[4]))
        {
            Util.Message("Usage: /title <player> times <fadeIn> <stay> <fadeOut>", sender);
            return;
        }

        try
        {
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            PacketContainer container = new PacketContainer(TitlePacket.packetType, new TitlePacket(TitlePacket.ACTION_TIMES, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])));

            manager.sendServerPacket(player, container);
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        catch (JsonParseException e)
        {
            sender.sendMessage("Invalid json!");
        }
    }


    private static class TitlePacket extends Packet
    {
        public static final PacketType packetType = new PacketType(PacketType.Protocol.PLAY, PacketType.Sender.SERVER, 69, -1);

        public static final int ACTION_TITLE = 0;
        public static final int ACTION_SUBTITLE = 1;
        public static final int ACTION_TIMES = 2;
        public static final int ACTION_CLEAR = 3;
        public static final int ACTION_RESET = 4;

        private int action;
        private String text;
        private int fadeIn;
        private int stay;
        private int fadeOut;

        public TitlePacket(int action)
        {
            this.action = action;
        }

        public TitlePacket(int action, String text)
        {
            this.action = action;
            this.text = text;
        }

        public TitlePacket(int action, int fadeIn, int stay, int fadeOut)
        {
            this.action = action;
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }


        @Override
        public void a(PacketDataSerializer packetDataSerializer) throws IOException
        {
        }

        @Override
        public void b(PacketDataSerializer packetDataSerializer) throws IOException
        {
            packetDataSerializer.b(action);

            if (action == ACTION_TITLE || action == ACTION_SUBTITLE)
            {
                packetDataSerializer.a(text);
            }
            else if (action == ACTION_TIMES)
            {
                packetDataSerializer.writeInt(fadeIn);
                packetDataSerializer.writeInt(stay);
                packetDataSerializer.writeInt(fadeOut);
            }
        }

        @Override
        public void handle(PacketListener packetListener)
        {
        }
    }
}
