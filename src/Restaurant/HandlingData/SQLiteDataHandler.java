package Restaurant.HandlingData;

import Restaurant.Properties.Dishes.AppetizerDish;
import Restaurant.Properties.Dishes.DesertDish;
import Restaurant.Properties.Dishes.Dish;
import Restaurant.Properties.Dishes.MainCourseDish;
import Restaurant.Properties.Food;
import Restaurant.Properties.Tables;
import Restaurant.Useres.Clients.Bills.Bill;
import Restaurant.Useres.Clients.Client;
import Restaurant.Useres.Clients.Order;
import Restaurant.Useres.Clients.VipClient;
import Restaurant.Useres.Cooker;
import Restaurant.Useres.Manager;
import Restaurant.Useres.Users;
import Restaurant.Useres.Waiter;
import Restaurant.Vectors.BillsVector;
import Restaurant.Vectors.DishesVector;
import Restaurant.Vectors.TablesVector;
import Restaurant.Vectors.UsersVector;
import RestaurantGUI.ProjectSystem.StyleMode.DarkMode;
import RestaurantGUI.ProjectSystem.StyleMode.LightMode;
import RestaurantGUI.ProjectSystem.StyleMode.StyleMode;

import java.sql.*;
import java.util.Vector;

public class SQLiteDataHandler {
    
    // Load data methods
    public static StyleMode loadStyleMode() {
        StyleMode styleMode = null;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT style_mode FROM settings ORDER BY id DESC LIMIT 1")) {
            
            if (rs.next()) {
                String mode = rs.getString("style_mode");
                if ("dark".equals(mode)) {
                    styleMode = new DarkMode();
                } else {
                    styleMode = new LightMode();
                }
            } else {
                // Default to light mode if no settings found
                styleMode = new LightMode();
            }
        } catch (SQLException e) {
            System.err.println("Error loading style mode: " + e.getMessage());
            // Default to light mode on error
            styleMode = new LightMode();
        }
        
        return styleMode;
    }

    public static UsersVector loadUsers() {
        UsersVector users = new UsersVector(25);
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                
                Users user = null;
                switch (role) {
                    case "Manager":
                        user = new Manager(username, username, password);
                        break;
                    case "Cooker":
                        user = new Cooker(username, username, password);
                        break;
                    case "Waiter":
                        user = new Waiter(username, username, password);
                        break;
                    case "Client":
                        user = new Client(username, username, password);
                        break;
                    case "VipClient":
                        double discount = rs.getDouble("vip_discount");
                        user = new VipClient(username, username, password, (int)discount);
                        break;
                }                  if (user != null) {
                    // Set client table if applicable
                    if (user instanceof Client) {
                        int tableId = rs.getInt("table_id");
                        // Only check rs.wasNull() right after getting the value
                        if (!rs.wasNull() && tableId > 0) {
                            // We'll need to load tables later and assign them
                            // This is handled below in assignTablesToClients
                        }
                    }
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        
        return users;
    }    public static TablesVector loadTables() {
        TablesVector tables = new TablesVector(25);
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tables")) {
              while (rs.next()) {
                // Skipping unused ID
                int tableNumber = rs.getInt("table_number");
                int capacity = rs.getInt("capacity");
                boolean isReserved = rs.getBoolean("is_reserved");
                
                // Create table with proper constructor and set booked status
                Tables table = new Tables(tableNumber, capacity, false);
                table.setBooked(isReserved);
                
                tables.add(table);
            }
        } catch (SQLException e) {
            System.err.println("Error loading tables: " + e.getMessage());
        }
        
        return tables;
    }public static void assignTablesToClients(UsersVector users, TablesVector tables) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT u.username, t.table_number FROM users u " +
                                            "JOIN tables t ON u.table_id = t.id " +
                                            "WHERE u.role IN ('Client', 'VipClient') AND u.table_id IS NOT NULL")) {
              while (rs.next()) {
                String username = rs.getString("username");
                int tableNumber = rs.getInt("table_number");
                
                // Find the client and the table
                Client client = null;
                Tables table = null;
                  for (int i = 0; i < users.getLength(); i++) {
                    if (users.get(i) instanceof Client && users.get(i).getUserName().equals(username)) {
                        client = (Client) users.get(i);
                        break;
                    }
                }
                
                for (int i = 0; i < tables.getLength(); i++) {
                    if (tables.get(i).getTableNumber() == tableNumber) {
                        table = tables.get(i);
                        break;
                    }
                }
                
                // Assign table to client
                if (client != null && table != null) {
                    client.setTable(table);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error assigning tables to clients: " + e.getMessage());
        }
    }    public static Food loadFood() {
        Food food = new Food(3, 25);
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM dishes")) {
              while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                // description variable is not used but keeping for future use
                // String description = rs.getString("description");
                String category = rs.getString("category");
                
                Dish dish = null;
                int categoryIndex = -1;
                
                switch (category) {
                    case "Appetizer":
                        dish = new AppetizerDish(name, (int)price);
                        categoryIndex = 0;
                        break;
                    case "MainCourse":
                        dish = new MainCourseDish(name, (int)price);
                        categoryIndex = 1;
                        break;
                    case "Desert":
                        dish = new DesertDish(name, (int)price);
                        categoryIndex = 2;
                        break;
                }
                
                if (dish != null && categoryIndex >= 0) {
                    food.addDish(dish, categoryIndex);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading food: " + e.getMessage());
        }
        
        return food;
    }    public static BillsVector loadBills(UsersVector users) {
        BillsVector bills = new BillsVector(25);
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT b.id, b.total_amount, b.bill_date, u.username " +
                                           "FROM bills b JOIN users u ON b.client_id = u.id")) {
              while (rs.next()) {
                // Skipping unused ID
                double totalAmount = rs.getDouble("total_amount");
                Timestamp date = rs.getTimestamp("bill_date");
                String username = rs.getString("username");
                
                // Find the client
                Client client = null;                for (int i = 0; i < users.getLength(); i++) {
                    Users currentUser = users.get(i);
                    if (currentUser instanceof Client && currentUser.getUserName().equals(username)) {
                        client = (Client) currentUser;
                        break;
                    }
                }
                
                if (client != null) {
                    // Create a new bill with the proper constructor
                    Bill bill = new Bill(client.getName(), date.toString(), (float)totalAmount, new DishesVector(0));
                    bills.add(bill);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading bills: " + e.getMessage());
        }
        
        return bills;
    }

    public static Vector<Order> loadOrders(UsersVector users, Food food) {
        Vector<Order> orders = new Vector<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT o.id, o.quantity, o.status, u.username, d.name " +
                                           "FROM orders o " +
                                           "JOIN users u ON o.client_id = u.id " +
                                           "JOIN dishes d ON o.dish_id = d.id")) {
              while (rs.next()) {
                // These variables are not currently used, but kept for future use
                // int id = rs.getInt("id");
                // int quantity = rs.getInt("quantity");
                // String status = rs.getString("status");
                // String dishName = rs.getString("name");
                
                String username = rs.getString("username");
                
                // Find the client
                Client client = null;                for (int i = 0; i < users.getLength(); i++) {
                    Users currentUser = users.get(i);
                    if (currentUser instanceof Client && currentUser.getUserName().equals(username)) {
                        client = (Client) currentUser;
                        break;
                    }
                }
                
                if (client != null) {
                    // Create a new order with the client's name and table number
                    int tableNumber = client.getTable() != null ? client.getTable().getTableNumber() : 0;
                    Order order = new Order(client.getName(), tableNumber);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
        
        return orders;
    }

    // Save data methods
    public static void saveStyleMode(StyleMode styleMode) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO settings (style_mode) VALUES (?)")) {
            
            String mode = styleMode instanceof DarkMode ? "dark" : "light";
            pstmt.setString(1, mode);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving style mode: " + e.getMessage());
        }
    }

    public static void saveUsers(UsersVector users) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing users
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM users");
            }
              // Insert users
            try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO users (username, password, role, vip_discount, table_id) VALUES (?, ?, ?, ?, ?)")) {
                
                for (int i = 0; i < users.getLength(); i++) {
                    Users user = users.get(i);
                    pstmt.setString(1, user.getUserName());
                    pstmt.setString(2, user.getPassword());
                    pstmt.setString(3, user.getRole());                    if (user instanceof VipClient) {
                        // VipClient vipClient = (VipClient) user; - unused variable
                        pstmt.setDouble(4, 10); // Assuming a fixed discount value, or use a method that exists
                    } else {
                        pstmt.setNull(4, Types.DOUBLE);
                    }
                      if (user instanceof Client && ((Client) user).getTable() != null) {
                        // Will need to find table ID
                        int tableNumber = ((Client) user).getTable().getTableNumber();
                        
                        try (Statement tableStmt = conn.createStatement();
                             ResultSet rs = tableStmt.executeQuery(
                                 "SELECT id FROM tables WHERE table_number = " + tableNumber)) {
                                
                            if (rs.next()) {
                                pstmt.setInt(5, rs.getInt("id"));
                            } else {
                                pstmt.setNull(5, Types.INTEGER);
                            }
                        }
                    } else {
                        pstmt.setNull(5, Types.INTEGER);
                    }
                    
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public static void saveTables(TablesVector tables) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing tables
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM tables");
            }
            
            // Insert tables
            try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO tables (table_number, capacity, is_reserved) VALUES (?, ?, ?)")) {
                  for (int i = 0; i < tables.getLength(); i++) {
                    Tables table = tables.get(i);
                    pstmt.setInt(1, table.getTableNumber());
                    pstmt.setInt(2, table.getNumberOfSeats());
                    pstmt.setBoolean(3, table.isBooked());
                    
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving tables: " + e.getMessage());
        }
    }

    public static void saveFood(Food food) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing dishes
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM dishes");
            }
              // Insert dishes
            try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO dishes (name, price, description, category) VALUES (?, ?, ?, ?)")) {
                
                // Process each dish category
                for (int categoryIndex = 0; categoryIndex < food.getLength(); categoryIndex++) {
                    DishesVector dishesVector = food.getDishesVector(categoryIndex);
                    String category = "";
                    
                    // Determine category name based on index
                    switch (categoryIndex) {
                        case 0:
                            category = "Appetizer";
                            break;
                        case 1:
                            category = "MainCourse";
                            break;
                        case 2:
                            category = "Desert";
                            break;
                    }
                    
                    // Process each dish in the category
                    for (int j = 0; j < dishesVector.getLength(); j++) {
                        Dish dish = dishesVector.get(j);
                        pstmt.setString(1, dish.getDishName());
                        pstmt.setDouble(2, dish.getPrice());
                        // Assuming description is stored in some way in Dish
                        pstmt.setString(3, "");  // Adjust if your Dish class has description
                            pstmt.setString(4, category);
                            pstmt.executeUpdate();
                        }
                    }
                }
        } catch (SQLException e) {
            System.err.println("Error saving food: " + e.getMessage());
        }
    }

    public static void saveBills(BillsVector bills) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing bills
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM bills");
            }
              // Insert bills
            try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO bills (client_id, total_amount, bill_date) VALUES ((SELECT id FROM users WHERE username = ?), ?, CURRENT_TIMESTAMP)")) {
                
                for (int i = 0; i < bills.getLength(); i++) {
                    Bill bill = bills.get(i);
                    // Assuming you have a way to get the username from the client name in the bill
                    pstmt.setString(1, bill.getClientName());
                    pstmt.setDouble(2, bill.getFloatBill());
                    
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving bills: " + e.getMessage());
        }
    }

    public static void saveOrders(Vector<Order> orders) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Clear existing orders
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM orders");
            }
              // Insert orders
            try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO orders (client_id, dish_id, quantity, status) " +
                "VALUES ((SELECT id FROM users WHERE username = ?), " +
                       "(SELECT id FROM dishes WHERE name = ?), ?, ?)")) {
                
                for (Order order : orders) {
                    // Adjust these based on your Order class structure
                    pstmt.setString(1, order.getClientName());
                    // Using placeholder values since the Order class structure seems different
                    pstmt.setString(2, "Unknown");  // Dish name would come from your order
                    pstmt.setInt(3, 1);  // Default quantity
                    pstmt.setString(4, "Pending");  // Default status
                    
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving orders: " + e.getMessage());
        }
    }
    
    public static void saveAllData(StyleMode styleMode, UsersVector users, TablesVector tables, 
                                  Food food, BillsVector bills, Vector<Order> orders) {
        // Make sure tables are saved first so we can reference them in users
        saveTables(tables);
        saveUsers(users);
        saveFood(food);
        saveBills(bills);
        saveOrders(orders);
        saveStyleMode(styleMode);
    }
}
