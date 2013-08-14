package com.mcnsa.flatcore.generation;

import net.minecraft.server.v1_6_R2.Block;
import net.minecraft.server.v1_6_R2.Material;
import net.minecraft.server.v1_6_R2.MaterialMapColor;

public class ImageGenerator {
	public static void generate()
	{
		
	}
	
	public static int getColor(int id)
	{
		Block block = Block.byId[id];
		Material material = block.material;
		MaterialMapColor color = material.H;
		
		return color.p;

	}
}
