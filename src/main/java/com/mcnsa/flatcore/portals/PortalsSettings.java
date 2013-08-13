package com.mcnsa.flatcore.portals;

import org.bukkit.Material;


public enum PortalsSettings {

	PORTAL_RATIO("PortalRatio", 8),
	INVESTIGATION_TOOL("InvestigationTool", Material.STICK.getId()),

	OVERWORLD_MIN_X("Overworld.PortalCreationBoundary.MinX", -10000),
	OVERWORLD_MIN_Z("Overworld.PortalCreationBoundary.MinZ", -10000),
	OVERWORLD_MAX_X("Overworld.PortalCreationBoundary.MaxX", 10000),
	OVERWORLD_MAX_Z("Overworld.PortalCreationBoundary.MaxZ", 10000),
	OVERWORLD_MIN_Y("Overworld.PortalCreationBoundary.MinY", 10),
	OVERWORLD_MAX_Y("Overworld.PortalCreationBoundary.MaxY", 100),
	OVERWORLD_MOVE_PORTALS_WITH_LOWER_Y("Overworld.MoveSpawnedPortals.WithLowerYThan", 20),
	OVERWORLD_MOVE_PORTALS_WITH_HIGHER_Y("Overworld.MoveSpawnedPortals.WithHigherYThan", 300),
	NETHER_MAX_X("Nether.PortalCreationBoundary.MaxX", 625),
	NETHER_MIN_X("Nether.PortalCreationBoundary.MinX", -625),
	NETHER_MAX_Z("Nether.PortalCreationBoundary.MaxZ", 625),
	NETHER_MIN_Z("Nether.PortalCreationBoundary.MinZ", -625),
	NETHER_MIN_Y("Nether.PortalCreationBoundary.MinY", 10),
	NETHER_MOVE_PORTALS_WITH_LOWER_Y("Nether.MoveSpawnedPortals.WithLowerYThan", 20),
	NETHER_MOVE_PORTALS_WITH_HIGHER_Y("Nether.MoveSpawnedPortals.WithHigherYThan", 120),
	NETHER_MAX_Y("Nether.PortalCreationBoundary.MaxY", 100),
	
	MESSAGE_CAN_MAKE_PORTAL("Messages.CanMakePortal", "&aYou can make a Nether portal here if you’d like"),
	MESSAGE_CANT_MAKE_PORTAL("Messages.CantMakePortal", "&cYou cannot make a Nether portal here because it would overlap a claim in <OtherDimension>."),
	
	SIGN_PORTAL_OUT_OF_BOUNDARIES("Signs.PortalOutOfBoundaries", "&cWarning![NEWLINE]Your portal[NEWLINE]Is out of[NEWLINE]world limit"),
	SIGN_OVERLAP_CLAIM("Signs.OverlapClaim", "&cWarning![NEWLINE]Foreign claim[NEWLINE]overlaps your[NEWLINE]destination");
	
	protected String string;
	protected Object def;
	
	private PortalsSettings(String string, Object def)
	{
		this.string = string;
		this.def = def;
	}

	public double doubleNumber()
	{
		return ((Number) PortalsModule.instance.config.get(string, def)).doubleValue();
	}
	
	public Integer integer()
	{
		return (Integer) PortalsModule.instance.config.get(string, def);
	}
	
	public String string()
	{
		return (String) PortalsModule.instance.config.get(string, def);
	}
	
	public static String getCommandDescription(String cmd, String def)
	{
		String path = "CommandDescriptions." + cmd;
		
		Object descO = PortalsModule.instance.config.get(path);
		if (descO == null)
		{
			PortalsModule.instance.config.set(path, "&a/chp " + cmd + " &8-&f " + def);
			PortalsModule.instance.saveConfig();
			descO = PortalsModule.instance.config.get(path);
		}
		
		return (String) descO;
		
	}
}
