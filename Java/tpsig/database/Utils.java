package tpsig.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.PGConnection;

/**
 * @author Sylvain B.
 * @version 1.0
 * 
 * Une classe utilitaire pour gérer la connexion à la base de données.
 */
public class Utils {
    private static Connection connection;
    private static String login = "bourgeel";
    private static String password = "bourgeel";
    
    
    public static Connection getConnection() {
        if (connection != null) return connection;
        return createConnection();
    }
    
    private static Connection createConnection() {
	try {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, "Loading PostgreSQL driver...");
	    Class.forName("org.postgresql.Driver");
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, "Trying to connect to winteam database...");
	    String url = "jdbc:postgresql://postgresql.ensimag.fr:5432/osm";
     //   String url = "jdbc:postgresql://localhost:5432/osm";
	    connection = DriverManager.getConnection(url, login, password);
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, "Connected.");
	    ((PGConnection) connection).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
	    ((PGConnection) connection).addDataType("box3d",Class.forName("org.postgis.PGbox3d"));
	} catch (SQLException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "Connection error: {0}", e.toString());
	} catch (ClassNotFoundException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "Error while loading postgis extensions: {0}", e.toString());
	}
	return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
