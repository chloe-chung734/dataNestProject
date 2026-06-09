package delivery.service;

import delivery.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.Pattern;

public final class CustomerService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+82 10-[0-9]{4}-[0-9]{4}$");

    private static Scanner scanner = new Scanner(System.in);
    
    private CustomerService() {}
    
    // MENU 1: Insert Customer
    public static void insertCustomer() {
        System.out.println("\n=== INSERT CUSTOMER ===");
        System.out.println("Phone format: +82 10-XXXX-XXXX (e.g. +82 10-1234-5678)");
        System.out.println("Gender: 1=Male, 2=Female, 3=Other");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String firstName = getRequiredStringInput("Enter first name: ");
            String lastName = getRequiredStringInput("Enter last name: ");
            String email = getValidEmailInput("Enter email: ");
            String phone = getValidPhoneInput("Enter phone: ");
            String city = getRequiredStringInput("Enter city: ");
            int age = getIntInputInRange("Enter age: ", 0, 120);
            String gender = getGenderInput();

            conn.setAutoCommit(false);

            try {
                String sql = "INSERT INTO customer (first_name, last_name, email, phone, city, age, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
                int customerId;
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.setString(3, email);
                    stmt.setString(4, phone);
                    stmt.setString(5, city);
                    stmt.setInt(6, age);
                    stmt.setString(7, gender);

                    if (stmt.executeUpdate() == 0) {
                        conn.rollback();
                        System.out.println("Customer registration failed.");
                        return;
                    }

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            System.out.println("Customer registration failed.");
                            return;
                        }
                        customerId = keys.getInt(1);
                    }
                }

                String historySql = "INSERT INTO customer_demographic_history (customer_id, city, age_range, gender, start_date, end_date) VALUES (?, ?, ?, ?, CURRENT_DATE, NULL)";
                try (PreparedStatement stmt = conn.prepareStatement(historySql)) {
                    stmt.setInt(1, customerId);
                    stmt.setString(2, city);
                    stmt.setString(3, getAgeRange(age));
                    stmt.setString(4, gender);
                    stmt.executeUpdate();
                }

                conn.commit();
                System.out.println("\n✓ Customer registered successfully! (ID: " + customerId + ")");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error registering customer: " + e.getMessage());
        }
    }
    
    // MENU 7: Update Customer Information
    public static void updateCustomer() {
        System.out.println("\n=== UPDATE CUSTOMER ===");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int customerId = getIntInput("Enter customer ID to update: ");
            
            // Check if customer exists
            if (!customerExists(conn, customerId)) {
                System.out.println("Customer ID " + customerId + " not found.");
                return;
            }
            
            System.out.println("\nWhat would you like to update?");
            System.out.println("1. City");
            System.out.println("2. Age");
            System.out.println("3. Both");
            int choice = getIntInput("Choice: ");
            
            String newCity = null;
            Integer newAge = null;
            
            if (choice == 1 || choice == 3) {
                newCity = getStringInput("Enter new city: ");
            }
            if (choice == 2 || choice == 3) {
                newAge = getIntInput("Enter new age: ");
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Update customer table
            if (newCity != null) {
                String sql = "UPDATE customer SET city = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, newCity);
                    stmt.setInt(2, customerId);
                    stmt.executeUpdate();
                }
            }
            
            if (newAge != null) {
                String sql = "UPDATE customer SET age = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, newAge);
                    stmt.setInt(2, customerId);
                    stmt.executeUpdate();
                }
            }
            
            // Insert into demographic history 
            String ageRange = getAgeRange(newAge != null ? newAge : getCurrentAge(conn, customerId));
            String gender = getCurrentGender(conn, customerId);
            String city = newCity != null ? newCity : getCurrentCity(conn, customerId);
            
            String historySql = "INSERT INTO customer_demographic_history (customer_id, city, age_range, gender, start_date, end_date) VALUES (?, ?, ?, ?, CURRENT_DATE, NULL)";
            try (PreparedStatement stmt = conn.prepareStatement(historySql)) {
                stmt.setInt(1, customerId);
                stmt.setString(2, city);
                stmt.setString(3, ageRange);
                stmt.setString(4, gender);
                stmt.executeUpdate();
            }
            
            conn.commit();
            System.out.println("\n✓ Customer updated successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
        }
    }
    
    // MENU 9: Delete Customer
    public static void deleteCustomer() {
        System.out.println("\n=== DELETE CUSTOMER ===");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int customerId = getIntInput("Enter customer ID to delete: ");
            
            if (!customerExists(conn, customerId)) {
                System.out.println("Customer ID " + customerId + " not found.");
                return;
            }
            
            String confirm = getStringInput("WARNING: This will delete all orders for this customer. Continue? (yes/no): ");
            
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Delete cancelled.");
                return;
            }
            
            conn.setAutoCommit(false);
            
            // Delete customer
            String sql = "DELETE FROM customer WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    conn.commit();
                    System.out.println("\n✓ Customer deleted successfully!");
                } else {
                    conn.rollback();
                    System.out.println("Delete failed.");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
        }
    }
    
    // HELPER METHODS 
    
    private static boolean customerExists(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT 1 FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeQuery().next();
        }
    }
    
    private static String getCurrentCity(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT city FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            var rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("city");
        }
        return null;
    }
    
    private static int getCurrentAge(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT age FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            var rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("age");
        }
        return 0;
    }
    
    private static String getCurrentGender(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT gender FROM customer WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            var rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("gender");
        }
        return null;
    }
    
    private static String getAgeRange(int age) {
        int lower = (age / 10) * 10;
        int upper = lower + 9;
        return lower + "-" + upper;
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
        return scanner.nextLine().trim();
    }

    private static String getRequiredStringInput(String prompt) {
        while (true) {
            String value = getStringInput(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field is required.");
        }
    }

    private static String getValidEmailInput(String prompt) {
        while (true) {
            String email = getStringInput(prompt);
            if (EMAIL_PATTERN.matcher(email).matches()) {
                return email;
            }
            System.out.println("Invalid email. Example: name@example.com");
        }
    }

    private static String getValidPhoneInput(String prompt) {
        while (true) {
            String phone = getStringInput(prompt);
            if (PHONE_PATTERN.matcher(phone).matches()) {
                return phone;
            }
            System.out.println("Invalid phone. Use format +82 10-XXXX-XXXX");
        }
    }

    private static String getGenderInput() {
        while (true) {
            int choice = getIntInput("Enter gender (1=Male, 2=Female, 3=Other): ");
            switch (choice) {
                case 1 -> { return "Male"; }
                case 2 -> { return "Female"; }
                case 3 -> { return "Other"; }
                default -> System.out.println("Please enter 1, 2, or 3.");
            }
        }
    }

    private static int getIntInputInRange(String prompt, int min, int max) {
        while (true) {
            int value = getIntInput(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Enter a number between " + min + " and " + max + ".");
        }
    }
}
