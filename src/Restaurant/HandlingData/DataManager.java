package Restaurant.HandlingData;

import Restaurant.Properties.Food;
import Restaurant.Properties.Tables;
import Restaurant.Useres.Clients.Bills.Bill;
import Restaurant.Useres.Clients.Order;
import Restaurant.Useres.Users;
import Restaurant.Vectors.BillsVector;
import Restaurant.Vectors.TablesVector;
import Restaurant.Vectors.UsersVector;
import RestaurantGUI.ProjectSystem.StyleMode.StyleMode;

import java.util.Vector;

public class DataManager {

    public static void loadAllData() {
           // Load data from SQLite database
        StyleMode styleMode = SQLiteDataHandler.loadStyleMode();
        UsersVector users = SQLiteDataHandler.loadUsers();
        TablesVector tables = SQLiteDataHandler.loadTables();
        SQLiteDataHandler.assignTablesToClients(users, tables);
        Food food = SQLiteDataHandler.loadFood();
        BillsVector bills = SQLiteDataHandler.loadBills(users);
        Vector<Order> orders = SQLiteDataHandler.loadOrders(users, food);

        // Pass loaded data to LoadData class using static setters
        LoadData.setStyleMode(styleMode);
        LoadData.setUsers(users);
        LoadData.setTables(tables);
        LoadData.setFood(food);
        LoadData.setBills(bills);
        LoadData.setOrders(orders);
    }

    public static void saveAllData() {
     
        // Retrieve data from SaveData class using static getters
        StyleMode styleMode = SaveData.getStyleMode();
        UsersVector users = SaveData.getUsers();
        TablesVector tables = SaveData.getTables();
        Food food = SaveData.getFood();
        BillsVector bills = SaveData.getBills();
        Vector<Order> orders = SaveData.getOrders();

        // Save data to SQLite database
        SQLiteDataHandler.saveAllData(styleMode, users, tables, food, bills, orders);
    }
}
