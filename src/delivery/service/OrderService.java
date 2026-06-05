package delivery.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;

public final class OrderService {

    private OrderService() { }

    public static void insertOrder(Connection conn, Scanner scanner) {
        System.out.println("\n=== [Menu 2] Place New Order ===");

        System.out.print("Enter Customer ID: ");
        int customerId = scanner.nextInt();

        System.out.print("Enter Restaurant ID: ");
        int restaurantId = scanner.nextInt();
        scanner.nextLine(); // clear buffer

        Timestamp orderTimestamp = new Timestamp(System.currentTimeMillis());

        String sql = "INSERT INTO ewha.orders (customer_id, restaurant_id, order_timestamp) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, restaurantId);
            pstmt.setTimestamp(3, orderTimestamp);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Success: Order recorded successfully!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database Error: " + e.getMessage());
        }
    }

    public static void searchCustomerOrders() {
        throw new UnsupportedOperationException("OrderService.searchCustomerOrders - not implemented yet");
    }

    public static void deleteOrder() {
        throw new UnsupportedOperationException("OrderService.deleteOrder - not implemented yet");
    }
}