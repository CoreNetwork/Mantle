package com.mcnsa.flatcore.generation;

import net.minecraft.server.v1_6_R2.Block;
import net.minecraft.server.v1_6_R2.Material;
import net.minecraft.server.v1_6_R2.MaterialMapColor;

import com.mcnsa.flatcore.FCLog;

public class ImageGenerator {
	public static void generate()
	{
		
	}
	
	public static int getColor(int id)
	{
		Block block = Block.byId[id];
		Material material = block.material;
		MaterialMapColor color = material.H;

		FCLog.info(Integer.toString(color.p));
		FCLog.info(Integer.toHexString(color.p));
		
		return color.p;

	}
}
