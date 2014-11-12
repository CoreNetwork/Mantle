package us.corenetwork.mantle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
public class IO {
    private static Connection connection;
    public static YamlConfiguration config;
        
    public static synchronized Connection getConnection() {
    	if (connection == null) connection = createConnection();
    	return connection;
    }
    private static Connection createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection ret = DriverManager.getConnection("jdbc:sqlite:" +  new File(MantlePlugin.instance.getDataFolder().getPath(), "data.sqlite").getPath());
            ret.setAutoCommit(false);
            return ret;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
   public static synchronized void freeConnection() {
		Connection conn = getConnection();
        if(conn != null) {
            try {
            	conn.close();
            	conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void LoadSettings()
	{
    	try {
    		config = new YamlConfiguration();

    		if (!new File(MantlePlugin.instance.getDataFolder(),"config.yml").exists()) config.save(new File(MantlePlugin.instance.getDataFolder(),"config.yml"));

    		config.load(new File(MantlePlugin.instance.getDataFolder(),"config.yml"));
	    	for (Setting s : Setting.values())
	    	{
	    		if (config.get(s.getString()) == null && s.getDefault() != null) config.set(s.getString(), s.getDefault());
	    	}
	    		    	
	    	saveConfig();
	    	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
    
    public static void saveConfig()
    {
    	try {
			config.save(new File(MantlePlugin.instance.getDataFolder(),"config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
       
    public static void PrepareDB()
    {
    	Connection conn;
        Statement st = null;
        try {
            conn = IO.getConnection();//            {
        	st = conn.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS regeneration_structures (ID INTEGER PRIMARY KEY NOT NULL, StructureName STRING NOT NULL, Schematic STRING NOT NULL, World STRING, CornerX INTEGER, CornerZ INTEGER, PastingY INTEGER, SizeX INTEGER, SizeZ INTEGER, LastCheck INTEGER DEFAULT 0, LastRestore INTEGER DEFAULT 0, InspectionStatus INTEGER DEFAULT 0)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS chests (ID INTEGER PRIMARY KEY NOT NULL, LootTable STRING, Interval INTEGER, PerPlayer INTEGER, World STRING, X INTEGER, Y INTEGER, Z INTEGER, StructureID INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS playerChests (ID INTEGER, PlayerUUID STRING, LastAccess INTEGER, Restocks INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS chestInventory (ID INTEGER, PlayerUUID STRING, Slot INTEGER, ItemID INTEGER, Damage INTEGER, Amount INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS horseSpeeds (ID STRING, Speed REAL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS hydration (PlayerUUID STRING, Hydration REAL, Saturation REAL, FatigueLevel INTEGER, FatigueLevelStart INTEGER, DeliveredMessages STRING)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS ignoredSlimeChunks (X INTEGER, Y INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS playerCategory (PlayerUUID STRING, Category STRING, TimesFound INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS playerVillage (PlayerUUID STRING, StructureID INTEGER, diminishVillage REAL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS playerTotal (PlayerUUID STRING, diminishTotal REAL, CompassCategory STRING, CompassChestID INTEGER)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS `playerCategory_idx` on `playerCategory` (`PlayerUUID`, `Category`);");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS `playerVillage_idx` on `playerVillage` (`PlayerUUID`, `StructureID`);");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS `playerTotal_idx` on `playerTotal` (`PlayerUUID`);");
        	conn.commit();
            st.close();
        } catch (SQLException e) {
            MantlePlugin.log.log(Level.SEVERE, "[Mantle]: Error while creating tables! - " + e.getMessage());
            e.printStackTrace();
    }
        UpdateDB();
    }
    
    public static void UpdateDB()
    {
    	Update("SELECT InspectionStatus FROM regeneration_structures LIMIT 1", "ALTER TABLE regeneration_structures ADD InspectionStatus INTEGER DEFAULT 0");
    }
        
    public static void Update(String check, String sql)
    {
    	try
    	{
    		Statement statement = getConnection().createStatement();
			statement.executeQuery(check);
			statement.close();
    	}
    	catch(SQLException ex)
    	{
    		MantlePlugin.log.log(Level.INFO, "[Mantle] Updating database");
    		try {
    			String[] query;
    			query = sql.split(";");
            	Connection conn = getConnection();
    			Statement st = conn.createStatement();
    			for (String q : query)	
    				st.executeUpdate(q);
    			conn.commit();
    			st.close();
    		} catch (SQLException e) {
    			MantlePlugin.log.log(Level.SEVERE, "[Mantle] Error while updating tables to the new version - " + e.getMessage());
                e.printStackTrace();
    	}
        
	}
    	
    }
}
