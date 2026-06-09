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

    private static final double TAX_RATE = 0.10;

    private static Scanner scanner = new Scanner(System.in);

    private OrderService() {}

    private record OrderLine(int menuItemId, String itemName, int quantity, double unitPrice) {
        double lineTotal() {
            return quantity * unitPrice;
        }
    }

    // MENU 2: Insert Order
    public static void insertOrder() {
        System.out.println("\n=== INSERT ORDER ===");
        System.out.println("Add menu items one at a time. Enter 0 for menu item ID when finished.");

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

            List<OrderLine> lines = collectOrderLines(conn);
            if (lines.isEmpty()) {
                System.out.println("Order must include at least one menu item.");
                return;
            }

            double subtotal = lines.stream().mapToDouble(OrderLine::lineTotal).sum();
            double taxAmount = roundMoney(subtotal * TAX_RATE);
            double finalTotal = roundMoney(subtotal + taxAmount);

            System.out.printf("%nOrder summary:%n");
            for (OrderLine line : lines) {
                System.out.printf("  - %s x%d @ $%.2f = $%.2f%n",
                        line.itemName(), line.quantity(), line.unitPrice(), line.lineTotal());
            }
            System.out.printf("Subtotal: $%.2f%n", subtotal);
            System.out.printf("Tax (10%%): $%.2f%n", taxAmount);
            System.out.printf("Final total: $%.2f%n", finalTotal);

            String confirm = getStringInput("Confirm order? (yes/no): ");
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Order cancelled.");
                return;
            }

            conn.setAutoCommit(false);
            try {
                Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());
                int orderId = insertOrderHeader(conn, customerId, restaurantId, orderTimestamp);
                insertOrderItems(conn, orderId, lines);
                insertBill(conn, orderId, subtotal, taxAmount, finalTotal);
                conn.commit();
                System.out.println("\n✓ Order recorded successfully! (Order ID: " + orderId + ")");
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
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
        }
    }

    public static void searchCustomerOrders() {
        System.out.println("Search Customer Orders - Coming Soon (Analytics Team)");
    }

    private static List<OrderLine> collectOrderLines(Connection conn) throws SQLException {
        List<OrderLine> lines = new ArrayList<>();

        while (true) {
            int menuItemId = getIntInput("Menu item ID (0 to finish): ");
            if (menuItemId == 0) {
                break;
            }

            MenuItemInfo menuItem = getAvailableMenuItem(conn, menuItemId);
            if (menuItem == null) {
                System.out.println("Menu item ID " + menuItemId + " not found or unavailable.");
                continue;
            }

            int quantity = getPositiveIntInput("Quantity: ");
            lines.add(new OrderLine(menuItemId, menuItem.name(), quantity, menuItem.price()));
        }

        return lines;
    }

    private static int insertOrderHeader(Connection conn, int customerId, int restaurantId, Timestamp orderTimestamp)
            throws SQLException {
        String sql = "INSERT INTO orders (customer_id, restaurant_id, order_timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, restaurantId);
            stmt.setTimestamp(3, orderTimestamp);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Failed to create order.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to retrieve new order ID.");
                }
                return keys.getInt(1);
            }
        }
    }

    private static void insertOrderItems(Connection conn, int orderId, List<OrderLine> lines) throws SQLException {
        String sql = "INSERT INTO order_item (order_id, menu_item_id, quantity, item_price_at_order) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderLine line : lines) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, line.menuItemId());
                stmt.setInt(3, line.quantity());
                stmt.setDouble(4, line.unitPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void insertBill(Connection conn, int orderId, double subtotal, double taxAmount, double finalTotal)
            throws SQLException {
        String sql = "INSERT INTO bill (order_id, subtotal, tax_amount, final_total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setDouble(2, subtotal);
            stmt.setDouble(3, taxAmount);
            stmt.setDouble(4, finalTotal);
            stmt.executeUpdate();
        }
    }

    private record MenuItemInfo(String name, double price) {}

    private static MenuItemInfo getAvailableMenuItem(Connection conn, int menuItemId) throws SQLException {
        String sql = "SELECT item_name, price FROM menu_item WHERE id = ? AND is_available = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, menuItemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new MenuItemInfo(rs.getString("item_name"), rs.getDouble("price"));
            }
        }
        return null;
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

    private static boolean orderExists(Connection conn, int orderId) throws SQLException {
        String sql = "SELECT 1 FROM orders WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeQuery().next();
        }
    }

    private static double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
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

    private static int getPositiveIntInput(String prompt) {
        while (true) {
            int value = getIntInput(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Quantity must be greater than 0.");
        }
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
