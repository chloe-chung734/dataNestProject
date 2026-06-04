package delivery.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public final class CustomerService {

    private CustomerService() { }

    public static void insertCustomer(Connection conn, Scanner scanner) {
        System.out.println("\n=== [Menu 1] Register New Customer ===");

        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine();

        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine();

        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine();

        System.out.print("Enter City: ");
        String city = scanner.nextLine();

        System.out.print("Enter Age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // clear buffer

        System.out.print("Enter Gender: ");
        String gender = scanner.nextLine();

        String sql = "INSERT INTO ewha.customer (first_name, last_name, email, phone, city, age, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.setString(5, city);
            pstmt.setInt(6, age);
            pstmt.setString(7, gender);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Success: Customer registered perfectly!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database Error: " + e.getMessage());
        }
    }

    public static void updateCustomer() {
        throw new UnsupportedOperationException("CustomerService.updateCustomer - not implemented yet");
    }

    public static void deleteCustomer() {
        throw new UnsupportedOperationException("CustomerService.deleteCustomer - not implemented yet");
    }
}