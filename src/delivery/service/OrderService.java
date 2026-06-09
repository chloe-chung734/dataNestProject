package delivery.service;

import delivery.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class OrderService {
    
    private static Scanner scanner = new Scanner(System.in);
    
    private OrderService() {}
    
    private static final double TAX_RATE = 0.10;

    // MENU 2: Insert Order (orders + order_item + bill in one transaction)
    public static void insertOrder() {
        System.out.println("\n=== INSERT ORDER ===");

        try (Connection conn = DatabaseConnection.getConnection()) {
            int customerId = getIntInput("Enter customer ID: ");
            if (!customerExists(conn, customerId)) {
                System.out.println("Customer ID " + customerId + " not found.");
                return;
            }

            int restaurantId = getIntInput("Enter restaurant ID: ");
            if (!restaurantExists(conn, restaurantId)) {
                System.out.println("Restaurant ID " + restaurantId + " not found.");
                return;
            }

            List<LineItem> lineItems = new ArrayList<>();
            boolean addMore = true;
            while (addMore) {
                int menuItemId = getIntInput("Enter menu item ID: ");
                double price = getMenuItemPrice(conn, menuItemId);
                if (price < 0) {
                    System.out.println("Menu item ID " + menuItemId + " not found.");
                    return;
                }

                int quantity = getIntInput("Enter quantity: ");
                if (quantity <= 0) {
                    System.out.println("Quantity must be greater than 0.");
                    return;
                }

                lineItems.add(new LineItem(menuItemId, quantity, price));

                String again = getStringInput("Add another item? (y/n): ");
                addMore = again.equalsIgnoreCase("y");
            }

            if (lineItems.isEmpty()) {
                System.out.println("Order must contain at least one item.");
                return;
            }

            conn.setAutoCommit(false);
            try {
                Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());
                int orderId;

                String orderSql = "INSERT INTO orders (customer_id, restaurant_id, order_timestamp) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, customerId);
                    stmt.setInt(2, restaurantId);
                    stmt.setTimestamp(3, orderTimestamp);
                    stmt.executeUpdate();

                    ResultSet keys = stmt.getGeneratedKeys();
                    if (!keys.next()) {
                        conn.rollback();
                        System.out.println("Failed to create order.");
                        return;
                    }
                    orderId = keys.getInt(1);
                }

                double subtotal = 0;
                String itemSql = "INSERT INTO order_item (order_id, menu_item_id, quantity, item_price_at_order) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                    for (LineItem item : lineItems) {
                        stmt.setInt(1, orderId);
                        stmt.setInt(2, item.menuItemId);
                        stmt.setInt(3, item.quantity);
                        stmt.setDouble(4, item.price);
                        stmt.executeUpdate();
                        subtotal += item.quantity * item.price;
                    }
                }

                double taxAmount = subtotal * TAX_RATE;
                double finalTotal = subtotal + taxAmount;

                String billSql = "INSERT INTO bill (order_id, subtotal, tax_amount, final_total) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(billSql)) {
                    stmt.setInt(1, orderId);
                    stmt.setDouble(2, subtotal);
                    stmt.setDouble(3, taxAmount);
                    stmt.setDouble(4, finalTotal);
                    stmt.executeUpdate();
                }

                conn.commit();
                System.out.println("\n✓ Order recorded successfully!");
                System.out.printf("Order ID: %d | Subtotal: %.2f | Tax: %.2f | Total: %.2f%n",
                        orderId, subtotal, taxAmount, finalTotal);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error recording order: " + e.getMessage());
        }
    }

    private record LineItem(int menuItemId, int quantity, double price) {}
    
    // MENU 3: Search Customer Orders (uses order_details_view)
    public static void searchCustomerOrders() {
        System.out.println("\n=== SEARCH CUSTOMER ORDERS ===");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String email = getStringInput("Enter customer email to search orders: ");
            
            String sql = """
                SELECT order_id, order_timestamp, restaurant_name, item_name,
                       quantity, item_price_at_order, line_subtotal, final_total
                FROM order_details_view
                WHERE email = ?
                ORDER BY order_timestamp DESC
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                
                boolean found = false;
                System.out.printf("%n%-8s %-19s %-18s %-16s %-4s %-10s %-10s %-10s%n",
                        "Order#", "Date", "Restaurant", "Item", "Qty", "Price", "Line", "Bill");
                System.out.println("-".repeat(105));
                
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-8d %-19s %-18s %-16s %-4d %-10.2f %-10.2f %-10.2f%n",
                            rs.getInt("order_id"),
                            rs.getString("order_timestamp"),
                            rs.getString("restaurant_name"),
                            rs.getString("item_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("item_price_at_order"),
                            rs.getDouble("line_subtotal"),
                            rs.getDouble("final_total"));
                }
                
                if (!found) {
                    System.out.println("No orders found for email: " + email);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching orders: " + e.getMessage());
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
    
    private static boolean customerExists(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT 1 FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeQuery().next();
        }
    }

    private static boolean restaurantExists(Connection conn, int restaurantId) throws SQLException {
        String sql = "SELECT 1 FROM restaurants WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            return stmt.executeQuery().next();
        }
    }

    private static double getMenuItemPrice(Connection conn, int menuItemId) throws SQLException {
        String sql = "SELECT price FROM menu_item WHERE id = ? AND is_available = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuItemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        }
        return -1;
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
