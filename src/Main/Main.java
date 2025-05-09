package Main;

import Restaurant.HandlingData.SaveData;
import Restaurant.HandlingData.SQLiteDataHandler;
import Restaurant.HandlingData.DatabaseConnection;
import Restaurant.Properties.Food;
import Restaurant.Useres.Clients.Client;
import Restaurant.Useres.Clients.Order;
import Restaurant.Useres.Cooker;
import Restaurant.Useres.Manager;
import Restaurant.Useres.Users;
import Restaurant.Useres.Waiter;
import Restaurant.Vectors.BillsVector;
import Restaurant.Vectors.TablesVector;
import Restaurant.Vectors.UsersVector;
import RestaurantGUI.ProjectSystem.StyleMode.SetStyle;
import RestaurantGUI.ProjectSystem.StyleMode.StyleMode;
import RestaurantGUI.UsersGUI.ClientGUI.ChoosingTableGUI;
import RestaurantGUI.UsersGUI.ClientGUI.ClientGUI;
import RestaurantGUI.LoginGUI.Login;
import RestaurantGUI.UsersGUI.CookerGUI;
import RestaurantGUI.UsersGUI.ManagerGUI;
import RestaurantGUI.UsersGUI.WaiterGUI;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Vector;

public class Main extends Application {

    private StyleMode styleMode;
    private Login login;
    private UsersVector usersVector;
    private Food food;
    private TablesVector tablesVector;
    private BillsVector billsVector;
    private ArrayList<Order> orders; // Use ArrayList instead of Vector

    @Override
    public void start(Stage primaryStage){
        // Initialize and load data from SQLite database
        try {
            // Get database connection to initialize it
            DatabaseConnection.getConnection();
            
            // Load data from SQLite
            styleMode = SQLiteDataHandler.loadStyleMode();
            usersVector = SQLiteDataHandler.loadUsers();
            tablesVector = SQLiteDataHandler.loadTables();
            food = SQLiteDataHandler.loadFood();
            
            // Assign tables to clients after loading both tables and users
            SQLiteDataHandler.assignTablesToClients(usersVector, tablesVector);
            
            billsVector = SQLiteDataHandler.loadBills(usersVector);
            orders = new ArrayList<>(SQLiteDataHandler.loadOrders(usersVector, food));
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            // Create empty data containers in case of error
            styleMode = null;
            usersVector = new UsersVector(25);
            tablesVector = new TablesVector(25);
            food = new Food(3, 25);
            billsVector = new BillsVector(25);
            orders = new ArrayList<>();
        }

        if (styleMode == null)
            styleMode = new SetStyle().show();

        // Keep backward compatibility with XML saver
        String path = "src\\Restaurant\\HandlingData\\data.xml";
        SaveData saver = new SaveData(path, styleMode, usersVector, tablesVector, food, billsVector, new Vector<>(orders));
        
        // Save data to SQLite as well
        SQLiteDataHandler.saveAllData(styleMode, usersVector, tablesVector, food, billsVector, new Vector<>(orders));
        
        // Go to login user interface
        login = new Login(styleMode);
        login(saver, orders);
    }

    @Override
    public void stop() {
        // Close database connection when the application exits
        try {
            DatabaseConnection.closeConnection();
            System.out.println("Database connection closed successfully");
        } catch (Exception e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    public void login(SaveData saver, ArrayList<Order> orders){
        Users user = login.login(usersVector, saver);
        this.styleMode = login.getStyleMode();
        showDashBoard(user, saver, orders);
    }

    private void showDashBoard(Users user, SaveData saver, ArrayList<Order> orders){
        String role = user.getRole();
        switch (role){
            case "Manager":
                new ManagerGUI(styleMode).managerDashBoard((Manager) user, usersVector, tablesVector, billsVector, saver);
                break;

            case "Cooker":
                new CookerGUI(styleMode).show((Cooker)user, new Vector<>(orders), food, saver);
                break;

            case "Waiter":
                new WaiterGUI(styleMode).show((Waiter)user, tablesVector, saver);
                break;

            case "Client":
            case "VipClient":
                if (((Client) user).getTable() == null) {
                    if (new ChoosingTableGUI(styleMode).show(tablesVector, (Client) user, saver))
                        new ClientGUI(styleMode).show((Client) user, food, billsVector, new Vector<>(orders), saver);
                }

                else
                    new ClientGUI(styleMode).show((Client) user, food, billsVector, new Vector<>(orders), saver);

                break;

            default:
                System.exit(-1);
        }

        login(saver, orders);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
