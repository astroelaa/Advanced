package Restaurant.HandlingData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseConnection {
    private static final String DB_PATH = "restaurant.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Create database file if it doesn't exist
            File dbFile = new File(DB_PATH);
            boolean needsInitialization = !dbFile.exists();
            
            // Create connection
            connection = DriverManager.getConnection(URL);
            
            // Initialize database if it's new
            if (needsInitialization) {
                initializeDatabase(connection);
            }
        }
        return connection;
    }
    
    private static void initializeDatabase(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Create users table
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "vip_discount REAL," +
                "table_id INTEGER" +
                ")");
        
        // Create tables table
        stmt.execute("CREATE TABLE IF NOT EXISTS tables (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "table_number INTEGER UNIQUE NOT NULL," +
                "capacity INTEGER NOT NULL," +
                "is_reserved BOOLEAN NOT NULL" +
                ")");
        
        // Create dishes table
        stmt.execute("CREATE TABLE IF NOT EXISTS dishes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE NOT NULL," +
                "price REAL NOT NULL," +
                "description TEXT," +
                "category TEXT NOT NULL" +
                ")");
        
        // Create orders table
        stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "client_id INTEGER," +
                "dish_id INTEGER," +
                "quantity INTEGER NOT NULL," +
                "status TEXT NOT NULL," +
                "order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (client_id) REFERENCES users(id)," +
                "FOREIGN KEY (dish_id) REFERENCES dishes(id)" +
                ")");
        
        // Create bills table
        stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "client_id INTEGER," +
                "total_amount REAL NOT NULL," +
                "bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (client_id) REFERENCES users(id)" +
                ")");
        
        // Create settings table
        stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "style_mode TEXT NOT NULL" +
                ")");
        
        stmt.close();
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}