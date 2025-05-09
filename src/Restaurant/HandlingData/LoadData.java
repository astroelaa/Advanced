package Restaurant.HandlingData;

import Restaurant.Properties.Dishes.Dish;
import Restaurant.Useres.Clients.Order;
import RestaurantGUI.ProjectSystem.StyleMode.StyleMode;
import Restaurant.Properties.Food;
import Restaurant.Vectors.BillsVector;
import Restaurant.Vectors.TablesVector;
import Restaurant.Vectors.UsersVector;
import java.util.Vector;

public final class LoadData {

    // Non-static instance variables (for backward compatibility)
    private String path;
    
    // Static fields to store data
    private static StyleMode styleMode;
    private static UsersVector users = new UsersVector(25);
    private static TablesVector tables = new TablesVector(25);
    private static Food food = new Food(3, 25);
    private static BillsVector bills = new BillsVector(25);
    private static Vector<Order> orders = new Vector<>();

    public LoadData(String path){
        this.path = path;
    }

    // Instance methods that delegate to static functionality
    public StyleMode getStyleMode() {
        return LoadData.styleMode;
    }

    public UsersVector getUsers() {
        return LoadData.users;
    }

    public BillsVector getBills() {
        return LoadData.bills;
    }

    public TablesVector getTables() {
        return LoadData.tables;
    }

    public Food getFood() {
        return LoadData.food;
    }
    
    public String getPath(){
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Vector<Order> getOrders() {
        return LoadData.orders;
    }
    
    // Static setters and getters for DataManager
    public static void setStyleMode(StyleMode styleMode) {
        LoadData.styleMode = styleMode;
    }

    public static void setUsers(UsersVector users) {
        LoadData.users = users;
    }

    public static void setTables(TablesVector tables) {
        LoadData.tables = tables;
    }

    public static void setFood(Food food) {
        LoadData.food = food;
    }

    public static void setBills(BillsVector bills) {
        LoadData.bills = bills;
    }

    public static void setOrders(Vector<Order> orders) {
        LoadData.orders = orders;
    }

    public static StyleMode getStaticStyleMode() {
        return styleMode;
    }

    public static UsersVector getStaticUsers() {
        return users;
    }

    public static TablesVector getStaticTables() {
        return tables;
    }

    public static Food getStaticFood() {
        return food;
    }

    public static BillsVector getStaticBills() {
        return bills;
    }

    public static Vector<Order> getStaticOrders() {
        return orders;
    }

    public void loadData() {
        // Use DataManager to load data from SQLite database
        DataManager.loadAllData();
    }
}
