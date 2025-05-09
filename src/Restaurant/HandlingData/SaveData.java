package Restaurant.HandlingData;

import Restaurant.Properties.Food;
import Restaurant.Useres.Clients.Order;
import RestaurantGUI.ProjectSystem.StyleMode.StyleMode;
import Restaurant.Vectors.BillsVector;
import Restaurant.Vectors.TablesVector;
import Restaurant.Vectors.UsersVector;
import java.util.Vector;

public final class SaveData {

    // Static fields to store data
    private static StyleMode styleMode;
    private static UsersVector users;
    private static TablesVector tables;
    private static Food food;
    private static BillsVector bills;
    private static Vector<Order> orders;

    public SaveData() {
        // Constructor without unused fields
    }

    // Constructor kept for backward compatibility
    public SaveData(String path, StyleMode styleMode, UsersVector users, TablesVector tables,
                    Food food, BillsVector bills, Vector<Order> orders) {
        SaveData.styleMode = styleMode;
        SaveData.users = users;
        SaveData.tables = tables;
        SaveData.food = food;
        SaveData.bills = bills;
        SaveData.orders = orders;
    }

    public void setStyleMode(StyleMode styleMode) {
        SaveData.styleMode = styleMode;
    }

    public void saveData() {
        // Use DataManager to save data to SQLite database
        DataManager.saveAllData();
    }

    // Static getters
    public static StyleMode getStyleMode() {
        return styleMode;
    }

    public static UsersVector getUsers() {
        return users;
    }

    public static TablesVector getTables() {
        return tables;
    }

    public static Food getFood() {
        return food;
    }

    public static BillsVector getBills() {
        return bills;
    }

    public static Vector<Order> getOrders() {
        return orders;
    }
}
