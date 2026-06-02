import java.sql.*;
import java.util.Scanner;

public class main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/ewha";
        String user = "root";
        String password = "yourpassword";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to database.");
            boolean running = true;

            while (running) {
                printMenu();
                String input = scanner.nextLine().trim();

                switch (input) {
                    case "1" -> MenuQueries.menuA(conn);
                    case "2" -> MenuQueries.menuB(conn);
                    case "3" -> MenuQueries.menuC(conn);
                    case "4" -> MenuQueries.menuD(conn);
                    case "0" -> {
                        System.out.println("Exiting. Goodbye.");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }

            }

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n========================================");
        System.out.println("         Delivery Analytics Menu        ");
        System.out.println("========================================");
        System.out.println(" 1. Sales before & after price change   ");
        System.out.println(" 2. Customer sales by demographic period ");
        System.out.println(" 3. Monthly revenue trend by item        ");
        System.out.println(" 4. Customer performance by age range    ");
        System.out.println(" 0. Exit                                 ");
        System.out.println("========================================");
        System.out.print("Select an option: ");
    }
}