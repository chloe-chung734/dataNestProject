package delivery.service;

import delivery.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;

public final class OrderService {
    
    private static Scanner scanner = new Scanner(System.in);
    
    private OrderService() {}
    
    // MENU 2: Insert Order
    public static void insertOrder() {
        System.out.println("\n=== INSERT ORDER ===");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int customerId = getIntInput("Enter customer ID: ");
            int restaurantId = getIntInput("Enter restaurant ID: ");
            Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());
            
            String sql = "INSERT INTO orders (customer_id, restaurant_id, order_timestamp) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                stmt.setInt(2, restaurantId);
                stmt.setTimestamp(3, orderTimestamp);
                
                if (stmt.executeUpdate() > 0) {
                    System.out.println("\n✓ Order recorded successfully!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error recording order: " + e.getMessage());
        }
    }
    
    // MENU 10: Delete Order
    public static void deleteOrder() {
        System.out.println("\n=== DELETE ORDER ===");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int orderId = getIntInput("Enter order ID to delete: ");
            
            if (!orderExists(conn, orderId)) {
                System.out.println("Order ID " + orderId + " not found.");
                return;
            }
            
            String confirm = getStringInput("WARNING: This will permanently delete this order. Continue? (yes/no): ");
            
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Delete cancelled.");
                return;
            }
            
            conn.setAutoCommit(false);
            
            // Delete order (CASCADE will handle order_item and bill)
            String sql = "DELETE FROM orders WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, orderId);
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    conn.commit();
                    System.out.println("\n✓ Order deleted successfully!");
                } else {
                    conn.rollback();
                    System.out.println("Delete failed.");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
        }
    }
    
    public static void searchCustomerOrders() {
        System.out.println("Search Customer Orders - Coming Soon (Analytics Team)");
    }
    
    private static boolean orderExists(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT 1 FROM orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeQuery().next();
        }
    }
    
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
    
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
