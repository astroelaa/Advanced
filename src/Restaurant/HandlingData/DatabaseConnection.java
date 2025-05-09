// FILEPATH: C:/Users/FADEL/Downloads/AdvancedProg project/AdvancedProg project/src/Restaurant/HandlingData/DatabaseConnection.java
package Restaurant.HandlingData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USERNAME = "system";
    private static final String PASSWORD = "123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}