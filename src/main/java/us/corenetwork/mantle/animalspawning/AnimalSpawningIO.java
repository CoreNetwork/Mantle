package us.corenetwork.mantle.animalspawning;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import us.corenetwork.mantle.MLog;
import us.corenetwork.mantle.MantlePlugin;
public class AnimalSpawningIO {
    private static Connection connection;
        
    public static synchronized Connection getConnection() {
    	if (connection == null) connection = createConnection();
    	return connection;
    }
    private static Connection createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection ret = DriverManager.getConnection("jdbc:sqlite:" +  new File(MantlePlugin.instance.getDataFolder().getPath(), "animal_chunks.sqlite").getPath());
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
    
       
    public static void PrepareDB()
    {
    	Connection conn;
        Statement st = null;
        try {
            conn = AnimalSpawningIO.getConnection();//            {
        	st = conn.createStatement();
        	st.executeUpdate("CREATE TABLE IF NOT EXISTS animal_chunks (X INTEGER, Z INTEGER, Spawned TINYINT)");
        	conn.commit();
            st.close();
        } catch (SQLException e) {
            MLog.severe("[Mantle]: Error while creating animal tables! - " + e.getMessage());
            e.printStackTrace();
    }
        UpdateDB();
    }
    
    public static void UpdateDB()
    {
    }
        
    public void Update(String check, String sql)
    {
    	try
    	{
    		Statement statement = getConnection().createStatement();
			statement.executeQuery(check);
			statement.close();
    	}
    	catch(SQLException ex)
    	{
    		MLog.info("Updating animal chunks database");
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
    			MLog.severe("Error while updating tables to the new version - " + e.getMessage());
                e.printStackTrace();
    	}
        
	}
    	
    }
}
