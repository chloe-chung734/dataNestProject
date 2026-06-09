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

    // MENU 4: Search Restaurant Sales (aggregates from order_details_view)
    public static void searchRestaurantSales() {
        System.out.println("\n=== SEARCH RESTAURANT SALES ===");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String city = getStringInput("Enter city to search restaurant sales: ");

            String sql = """
                SELECT restaurant_name,
                       COUNT(DISTINCT order_id) AS total_orders,
                       SUM(order_total)         AS total_revenue
                FROM (
                    SELECT DISTINCT order_id, restaurant_name, final_total AS order_total
                    FROM order_details_view
                    WHERE restaurant_city = ?
                ) restaurant_orders
                GROUP BY restaurant_name
                ORDER BY total_revenue DESC
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, city);
                ResultSet rs = stmt.executeQuery();

                boolean found = false;
                System.out.printf("%n%-22s %-14s %-14s%n", "Restaurant", "Orders", "Revenue");
                System.out.println("-".repeat(52));

                while (rs.next()) {
                    found = true;
                    System.out.printf("%-22s %-14d %-14.2f%n",
                            rs.getString("restaurant_name"),
                            rs.getInt("total_orders"),
                            rs.getDouble("total_revenue"));
                }

                if (!found) {
                    System.out.println("No sales found for city: " + city);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching restaurant sales: " + e.getMessage());
        }

        pressEnter();
    }

    // MENU 5: Analyze Price Change (uses v_item_sales_by_price_era)
    public static void priceChangeAnalysis() {
        System.out.println("\n========== PRICE CHANGE ANALYSIS ==========");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String itemName = getStringInput("Enter menu item name to compare sales before and after price change: ");

            String sql = """
                SELECT COALESCE(price_era, 'no price history') AS price_era,
                       era_old_price,
                       era_new_price,
                       price_changed_on,
                       COUNT(*)                    AS num_line_items,
                       SUM(quantity)               AS units_sold,
                       SUM(line_revenue)           AS total_revenue,
                       ROUND(AVG(line_revenue), 2) AS avg_line_value
                FROM v_item_sales_by_price_era
                WHERE item_name = ?
                GROUP BY COALESCE(price_era, 'no price history'),
                         era_old_price, era_new_price, price_changed_on
                ORDER BY price_changed_on, price_era DESC
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, itemName);
                ResultSet rs = stmt.executeQuery();

                boolean found = false;
                System.out.printf("%n%-8s %-12s %-12s %-12s %-8s %-8s %-12s %-12s%n",
                        "Era", "Old Price", "New Price", "Changed On",
                        "Lines", "Units", "Revenue", "Avg Line");
                System.out.println("-".repeat(90));

                while (rs.next()) {
                    found = true;
                    System.out.printf("%-8s %-12.2f %-12.2f %-12s %-8d %-8d %-12.2f %-12.2f%n",
                            rs.getString("price_era"),
                            rs.getDouble("era_old_price"),
                            rs.getDouble("era_new_price"),
                            rs.getString("price_changed_on"),
                            rs.getInt("num_line_items"),
                            rs.getInt("units_sold"),
                            rs.getDouble("total_revenue"),
                            rs.getDouble("avg_line_value"));
                }

                if (!found) {
                    System.out.println("No sales data found for menu item: " + itemName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error analyzing price change: " + e.getMessage());
        }

        pressEnter();
    }

    // MENU 6: Analyze Customer Demographics (uses v_customer_sales_by_demo)
    public static void analyzeCustomerDemographics() {
        System.out.println("\n========== CUSTOMER DEMOGRAPHICS ANALYSIS ==========");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String email = getStringInput("Enter customer email to view sales across demographic history: ");

            String sql = """
                SELECT COALESCE(demo_city, 'unknown') AS demo_city,
                       age_range,
                       gender,
                       demo_start,
                       demo_end,
                       demo_status,
                       COUNT(order_id)              AS num_orders,
                       SUM(final_total)             AS total_spent,
                       ROUND(AVG(final_total), 2)   AS avg_order_value
                FROM v_customer_sales_by_demo
                WHERE email = ?
                GROUP BY COALESCE(demo_city, 'unknown'), age_range, gender,
                         demo_start, demo_end, demo_status
                ORDER BY demo_start
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                boolean found = false;
                System.out.printf("%n%-10s %-10s %-8s %-12s %-12s %-12s %-8s %-12s %-10s%n",
                        "City", "Age Range", "Gender", "Demo Start", "Demo End",
                        "Status", "Orders", "Total Spent", "Avg Value");
                System.out.println("-".repeat(110));

                while (rs.next()) {
                    found = true;
                    String demoEnd = rs.getString("demo_end");
                    if (demoEnd == null) {
                        demoEnd = "NULL";
                    }
                    System.out.printf("%-10s %-10s %-8s %-12s %-12s %-12s %-8d %-12.2f %-10.2f%n",
                            rs.getString("demo_city"),
                            rs.getString("age_range"),
                            rs.getString("gender"),
                            rs.getString("demo_start"),
                            demoEnd,
                            rs.getString("demo_status"),
                            rs.getInt("num_orders"),
                            rs.getDouble("total_spent"),
                            rs.getDouble("avg_order_value"));
                }

                if (!found) {
                    System.out.println("No sales data found for email: " + email);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error analyzing demographics: " + e.getMessage());
        }

        pressEnter();
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static void pressEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
