package delivery.service;

import delivery.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public final class AdminService {
    
    private static Scanner scanner = new Scanner(System.in);
    
    private AdminService() {}
    
    // MENU 8: Update Menu Price
    public static void updateMenuPrice() {
        System.out.println("\n=== UPDATE MENU PRICE ===");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int menuId = getIntInput("Enter menu item ID to update: ");
            
            // Check if menu item exists and get current price
            double currentPrice = getCurrentPrice(conn, menuId);
            if (currentPrice == -1) {
                System.out.println("Menu item ID " + menuId + " not found.");
                return;
            }
            
            System.out.println("Current price: $" + currentPrice);
            double newPrice = getDoubleInput("Enter new price: ");
            
            if (newPrice <= 0) {
                System.out.println("Price must be greater than 0.");
                return;
            }
            
            if (newPrice == currentPrice) {
                System.out.println("New price is same as current price. No update needed.");
                return;
            }
            
            String confirm = getStringInput("Update price from $" + currentPrice + " to $" + newPrice + "? (yes/no): ");
            
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Update cancelled.");
                return;
            }
            
            conn.setAutoCommit(false);
            
            // Insert into price history
            String historySql = "INSERT INTO menu_price_history (menu_item_id, old_price, new_price, change_date) VALUES (?, ?, ?, CURRENT_DATE)";
            try (PreparedStatement stmt = conn.prepareStatement(historySql)) {
                stmt.setInt(1, menuId);
                stmt.setDouble(2, currentPrice);
                stmt.setDouble(3, newPrice);
                stmt.executeUpdate();
            }
            
            // Update menu item price
            String updateSql = "UPDATE menu_item SET price = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setDouble(1, newPrice);
                stmt.setInt(2, menuId);
                stmt.executeUpdate();
            }
            
            conn.commit();
            System.out.println("\n✓ Menu price updated successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error updating price: " + e.getMessage());
        }
    }
    
    private static double getCurrentPrice(Connection conn, int menuId) throws SQLException {
        String sql = "SELECT price FROM menu_item WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        }
        return -1;
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
    
    private static double getDoubleInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.print("Invalid. Enter a number: ");
            scanner.next();
        }
        double result = scanner.nextDouble();
        scanner.nextLine();
        return result;
    }
    
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}