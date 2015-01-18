package us.corenetwork.mantle.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import us.corenetwork.mantle.Util;

public class SignUtil
{
    /**
     * Convenience method to place sign on specified spot. It will automatically pick either wall sign or standing sign and automatically rotate it based on where surface to attach to is.
     * @param block Where should we place the sign.
     * @param message Message on the sign, lines separated by <b>[NEWLINE]</b>.
     */
    public static void placeSign(final Block block, final String message)
    {
        org.bukkit.material.Sign signData = new org.bukkit.material.Sign();
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH})
        {
            Block nextBlock = block.getRelative(face);
            if (nextBlock != null && nextBlock.getType().isSolid())
            {
                signData.setFacingDirection(face.getOppositeFace());

                break;
            }
        }

        Block belowBlock = block.getRelative(BlockFace.DOWN);
        if (belowBlock != null && belowBlock.getType().isSolid())
        {
            block.setTypeIdAndData(Material.SIGN_POST.getId(), signData.getData(), true);
        } else
        {
            block.setTypeIdAndData(Material.WALL_SIGN.getId(), signData.getData(), true);
        }

        Sign sign = (Sign) block.getState();
        //Rotate sign so it will be facing away from the wall

        populateSign(message, sign);

        sign.update();
    }

    /**
     * Convenience method to populate sign from one string.
     * @param message Message on the sign, lines separated by <b>[NEWLINE]</b>.
     * @param sign Sign to populate.
     */
    public static void populateSign(String message, Sign sign)
    {
        message = Util.applyColors(message);
        String[] lines = message.split("\\[NEWLINE\\]");

        int max = Math.min(4, lines.length);
        for (int i = 0; i < max; i++)
        {
            sign.setLine(i, lines[i]);
        }
    }

    /**
     * Convenience method to quickly check if sign has any colored text in it
     * @param sign Sign to check
     * @return true if sign has any colored text.
     */
    public static boolean doesSignHaveColors(Sign sign)
    {
        String colorSymbol = "\u00A7";
        for (String line : sign.getLines())
        {
            if (line.contains(colorSymbol))
            {
                return true;
            }
        }

        return false;
    }
}
