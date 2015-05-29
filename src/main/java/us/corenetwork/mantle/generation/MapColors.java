package us.corenetwork.mantle.generation;


import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.MaterialMapColor;

public class MapColors {
	
	public static final int colorMap[] = new int[1000];

	static
	{
		colorMap[0] = 0xffffff;
		colorMap[87] = 0xbe0000;
		colorMap[88] = 0x945e00;
		colorMap[89] = 0xfff832;
		colorMap[87] = 0xbe0000;
		colorMap[153] = 0xbe0000;
		colorMap[87] = 0xbe0000;
		colorMap[112] = 0xdc0303;
		colorMap[113] = 0xdc0303;
		colorMap[114] = 0xdc0303;
		colorMap[115] = 0xff0000;

	}
	
	public static int getColor(int id)
	{
		int cColor = colorMap[id];
		if (cColor != 0)
			return cColor;
		
		Block block = Block.getById(id); // NEEDS TESTING
		net.minecraft.server.v1_8_R3.Material material = block.getMaterial();
		MaterialMapColor color = material.r();
		
		return color.L;
	}
}
