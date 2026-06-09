package delivery.service;

import delivery.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public final class AnalysisService {

    private static Scanner scanner = new Scanner(System.in);

    private AnalysisService() {}

    // MENU 5: Analyze Price Change
    public static void priceChangeAnalysis() {
        System.out.println("\n========== PRICE CHANGE ANALYSIS ==========");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int menuId = getIntInput("Enter menu item ID to analyze: ");
            
            String itemName = getMenuItemName(conn, menuId);
            if (itemName == null) {
                System.out.println("Menu item ID " + menuId + " not found.");
                pressEnter();
                return;
            }
            
            String sql = "SELECT old_price, new_price, change_date FROM menu_price_history WHERE menu_item_id = ? ORDER BY change_date";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, menuId);
                ResultSet rs = stmt.executeQuery();
                
                boolean hasHistory = false;
                while (rs.next()) {
                    hasHistory = true;
                    double oldPrice = rs.getDouble("old_price");
                    double newPrice = rs.getDouble("new_price");
                    String changeDate = rs.getString("change_date");
                    
                    System.out.println("\n========================================");
                    System.out.println("Item: " + itemName + " (ID: " + menuId + ")");
                    System.out.println("Change Date: " + changeDate);
                    System.out.println("Old Price: $" + oldPrice + " → New Price: $" + newPrice);
                    System.out.println("----------------------------------------");
                    
                    double beforeRevenue = getRevenueBeforePriceChange(conn, menuId, oldPrice, changeDate);
                    int beforeCount = getOrderCountBeforePriceChange(conn, menuId, oldPrice, changeDate);
                    
                    double afterRevenue = getRevenueAfterPriceChange(conn, menuId, newPrice, changeDate);
                    int afterCount = getOrderCountAfterPriceChange(conn, menuId, newPrice, changeDate);
                    
                    System.out.println("Sales BEFORE price change: " + beforeCount + " orders, $" + String.format("%.2f", beforeRevenue));
                    System.out.println("Sales AFTER price change:  " + afterCount + " orders, $" + String.format("%.2f", afterRevenue));
                    
                    double difference = afterRevenue - beforeRevenue;
                    double percentChange = (beforeRevenue > 0) ? (difference / beforeRevenue) * 100 : 0;
                    System.out.println("Difference: " + (difference >= 0 ? "+" : "") + "$" + String.format("%.2f", difference) + " (" + String.format("%.1f", percentChange) + "%)");
                    System.out.println("========================================");
                }
                
                if (!hasHistory) {
                    System.out.println("No price change history found for this menu item.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error analyzing price change: " + e.getMessage());
        }
        
        pressEnter();
    }

    // MENU 6: Analyze Customer Demographics
    public static void analyzeCustomerDemographics() {
        System.out.println("\n========== CUSTOMER DEMOGRAPHICS ANALYSIS ==========");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int customerId = getIntInput("Enter customer ID to analyze: ");
            
            String customerName = getCustomerName(conn, customerId);
            if (customerName == null) {
                System.out.println("Customer ID " + customerId + " not found.");
                pressEnter();
                return;
            }
            
            String sql = "SELECT city, age_range, gender, start_date FROM customer_demographic_history WHERE customer_id = ? ORDER BY start_date";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                ResultSet rs = stmt.executeQuery();
                
                boolean hasHistory = false;
                while (rs.next()) {
                    hasHistory = true;
                    String city = rs.getString("city");
                    String ageRange = rs.getString("age_range");
                    String gender = rs.getString("gender");
                    String startDate = rs.getString("start_date");
                    
                    System.out.println("\n========================================");
                    System.out.println("Customer: " + customerName + " (ID: " + customerId + ")");
                    System.out.println("Effective Date: " + startDate);
                    System.out.println("City: " + city);
                    System.out.println("Age Range: " + ageRange);
                    System.out.println("Gender: " + gender);
                    System.out.println("----------------------------------------");
                    
                    double beforeRevenue = getRevenueBeforeDemographic(conn, customerId, startDate);
                    int beforeCount = getOrderCountBeforeDemographic(conn, customerId, startDate);
                    
                    double afterRevenue = getRevenueAfterDemographic(conn, customerId, startDate);
                    int afterCount = getOrderCountAfterDemographic(conn, customerId, startDate);
                    
                    System.out.println("Sales BEFORE demographic change: " + beforeCount + " orders, $" + String.format("%.2f", beforeRevenue));
                    System.out.println("Sales AFTER demographic change:  " + afterCount + " orders, $" + String.format("%.2f", afterRevenue));
                    
                    double difference = afterRevenue - beforeRevenue;
                    double percentChange = (beforeRevenue > 0) ? (difference / beforeRevenue) * 100 : 0;
                    System.out.println("Difference: " + (difference >= 0 ? "+" : "") + "$" + String.format("%.2f", difference) + " (" + String.format("%.1f", percentChange) + "%)");
                    System.out.println("========================================");
                }
                
                if (!hasHistory) {
                    System.out.println("No demographic change history found for this customer.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error analyzing demographics: " + e.getMessage());
        }
        
        pressEnter();
    }

    // HELPER METHODS (Price Analysis)
    
    private static String getMenuItemName(Connection conn, int menuId) throws SQLException {
        String sql = "SELECT item_name FROM menu_item WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("item_name");
        }
        return null;
    }
    
    private static double getRevenueBeforePriceChange(Connection conn, int menuId, double price, String changeDate) throws SQLException {
        String sql = "SELECT SUM(oi.quantity * oi.item_price_at_order) as total FROM order_item oi JOIN orders o ON oi.order_id = o.id WHERE oi.menu_item_id = ? AND oi.item_price_at_order = ? AND DATE(o.order_timestamp) < ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            stmt.setDouble(2, price);
            stmt.setString(3, changeDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        }
        return 0;
    }
    
    private static int getOrderCountBeforePriceChange(Connection conn, int menuId, double price, String changeDate) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM order_item oi JOIN orders o ON oi.order_id = o.id WHERE oi.menu_item_id = ? AND oi.item_price_at_order = ? AND DATE(o.order_timestamp) < ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            stmt.setDouble(2, price);
            stmt.setString(3, changeDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("count");
        }
        return 0;
    }
    
    private static double getRevenueAfterPriceChange(Connection conn, int menuId, double price, String changeDate) throws SQLException {
        String sql = "SELECT SUM(oi.quantity * oi.item_price_at_order) as total FROM order_item oi JOIN orders o ON oi.order_id = o.id WHERE oi.menu_item_id = ? AND oi.item_price_at_order = ? AND DATE(o.order_timestamp) >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            stmt.setDouble(2, price);
            stmt.setString(3, changeDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        }
        return 0;
    }
    
    private static int getOrderCountAfterPriceChange(Connection conn, int menuId, double price, String changeDate) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM order_item oi JOIN orders o ON oi.order_id = o.id WHERE oi.menu_item_id = ? AND oi.item_price_at_order = ? AND DATE(o.order_timestamp) >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            stmt.setDouble(2, price);
            stmt.setString(3, changeDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("count");
        }
        return 0;
    }
    
    // HELPER METHODS (Demographics Analysis) 
    
    private static String getCustomerName(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT first_name, last_name FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("first_name") + " " + rs.getString("last_name");
        }
        return null;
    }
    
    private static double getRevenueBeforeDemographic(Connection conn, int customerId, String startDate) throws SQLException {
        String sql = "SELECT SUM(b.final_total) as total FROM orders o JOIN bill b ON o.id = b.order_id WHERE o.customer_id = ? AND DATE(o.order_timestamp) < ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, startDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        }
        return 0;
    }
    
    private static int getOrderCountBeforeDemographic(Connection conn, int customerId, String startDate) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE customer_id = ? AND DATE(order_timestamp) < ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, startDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("count");
        }
        return 0;
    }
    
    private static double getRevenueAfterDemographic(Connection conn, int customerId, String startDate) throws SQLException {
        String sql = "SELECT SUM(b.final_total) as total FROM orders o JOIN bill b ON o.id = b.order_id WHERE o.customer_id = ? AND DATE(o.order_timestamp) >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, startDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        }
        return 0;
    }
    
    private static int getOrderCountAfterDemographic(Connection conn, int customerId, String startDate) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE customer_id = ? AND DATE(order_timestamp) >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, startDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("count");
        }
        return 0;
    }
    
    // INPUT HELPERS
    
    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid. Enter a number: ");
            scanner.next();
        }
        int result = scanner.nextInt();
        scanner.nextLine();
        return result;
    }
    
    private static void pressEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }


    // MENU 4: Search Restaurant Sales
    public static void searchRestaurantSales() {
        System.out.println("\n========== SEARCH RESTAURANT SALES ==========");

        try (Connection conn = DatabaseConnection.getConnection()) {
            int restaurantId = getIntInput("Enter restaurant ID: ");

            String restaurantName = getRestaurantName(conn, restaurantId);
            if (restaurantName == null) {
                System.out.println("Restaurant ID " + restaurantId + " not found.");
                pressEnter();
                return;
            }

            printRestaurantSalesSummary(conn, restaurantId, restaurantName);
            printRestaurantItemSales(conn, restaurantId);
            printRestaurantOrderHistory(conn, restaurantId);
        } catch (SQLException e) {
            System.err.println("Error searching restaurant sales: " + e.getMessage());
        }

        pressEnter();
    }

    private static void printRestaurantSalesSummary(Connection conn, int restaurantId, String restaurantName)
            throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT order_id) AS total_orders,
                       COALESCE(SUM(order_total), 0) AS total_revenue
                FROM (
                    SELECT order_id, MAX(final_total) AS order_total
                    FROM order_details_view
                    WHERE restaurant_id = ?
                    GROUP BY order_id
                ) order_totals
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("\nRestaurant: " + restaurantName + " (ID: " + restaurantId + ")");
                System.out.println("Total orders: " + rs.getInt("total_orders"));
                System.out.printf("Total revenue: $%.2f%n", rs.getDouble("total_revenue"));
            }
        }
    }

    private static void printRestaurantItemSales(Connection conn, int restaurantId) throws SQLException {
        String sql = """
                SELECT item_name,
                       SUM(quantity) AS quantity_sold,
                       SUM(quantity * item_price_at_order) AS item_revenue
                FROM order_details_view
                WHERE restaurant_id = ?
                GROUP BY menu_item_id, item_name
                ORDER BY item_revenue DESC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n----------------------------------------");
            System.out.println("Item sales breakdown:");
            boolean hasItems = false;
            while (rs.next()) {
                hasItems = true;
                System.out.printf("  - %s: %d sold, $%.2f revenue%n",
                        rs.getString("item_name"),
                        rs.getInt("quantity_sold"),
                        rs.getDouble("item_revenue"));
            }
            if (!hasItems) {
                System.out.println("  No items sold.");
            }
        }
    }

    private static void printRestaurantOrderHistory(Connection conn, int restaurantId) throws SQLException {
        String sql = """
                SELECT order_id, order_timestamp, MAX(final_total) AS final_total
                FROM order_details_view
                WHERE restaurant_id = ?
                GROUP BY order_id, order_timestamp
                ORDER BY order_timestamp
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n----------------------------------------");
            System.out.println("Order history:");
            boolean hasOrders = false;
            while (rs.next()) {
                hasOrders = true;
                System.out.printf("  - Order #%d on %s: $%.2f%n",
                        rs.getInt("order_id"),
                        rs.getString("order_timestamp"),
                        rs.getDouble("final_total"));
            }
            if (!hasOrders) {
                System.out.println("  No orders found.");
            }
            System.out.println("========================================");
        }
    }

    private static String getRestaurantName(Connection conn, int restaurantId) throws SQLException {
        String sql = "SELECT name FROM restaurants WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        }
        return null;
    }
}

