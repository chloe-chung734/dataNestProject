import java.sql.*;
import java.util.Scanner;
 
public class MenuQueries {
 
    private static final Scanner scanner = new Scanner(System.in);
 
    private static String getUserInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

//asks user to enter menu name to compare before & after prices
// Menu A — REQ6, uses v_item_sales_by_price_era
public static void menuA(Connection conn) throws SQLException {
    String itemName = getUserInput("Enter menu item name to compare sales before and after price change: ");

    String sql = """
        SELECT
            COALESCE(price_era, 'no price history') AS price_era,
            era_old_price,
            era_new_price,
            price_changed_on,
            COUNT(*)                    AS num_orders,
            SUM(quantity)               AS units_sold,
            SUM(line_revenue)           AS total_revenue,
            ROUND(AVG(line_revenue), 2) AS avg_order_value
        FROM v_item_sales_by_price_era
        WHERE item_name = ?
        GROUP BY COALESCE(price_era, 'no price history'), era_old_price, era_new_price, price_changed_on
        ORDER BY price_era DESC
        """;

    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString(1, itemName);
    ResultSet rs = ps.executeQuery();

    System.out.println("\n--- Sales Before & After Price Change: " + itemName + " ---");
        
    System.out.printf("%-20s %-12s %-12s %-18s %-12s %-12s %-15s %-15s%n",
        "Price Era", "Old Price", "New Price", "Changed On",
        "Num Orders", "Units Sold", "Total Revenue", "Avg Order Value");
    System.out.println("-".repeat(110));
 
    boolean hasResults = false;
    while (rs.next()) {
        hasResults = true;
        System.out.printf("%-20s %-12s %-12s %-18s %-12d %-12d %-15s %-15s%n",
            rs.getString("price_era"),
            rs.getString("era_old_price"),
            rs.getString("era_new_price"),
            rs.getString("price_changed_on"),
            rs.getInt("num_orders"),
            rs.getInt("units_sold"),
            rs.getString("total_revenue"),
            rs.getString("avg_order_value"));
    }
 
    if (!hasResults) {
        System.out.println("No results found for: " + itemName);
    }

rs.close();
ps.close();

}



//asks user for email to display customer sales by demoographic period
// Menu B — REQ6, uses v_customer_sales_by_demo
public static void menuB(Connection conn) throws SQLException {
    String email = getUserInput("Enter customer email to view sales across demographic history: ");


    String sql = """
        SELECT
            COALESCE(v.demo_city, 'unknown period') AS demo_city,
            v.age_range,
            v.gender,
            v.demo_start,
            v.demo_end,
            v.demo_status,
            COUNT(v.order_id)           AS num_orders,
            SUM(v.final_total)          AS total_spent,
            ROUND(AVG(v.final_total), 2) AS avg_order_value
        FROM v_customer_sales_by_demo v
        JOIN customer c ON v.customer_id = c.id
        WHERE c.email = ?
        GROUP BY COALESCE(v.demo_city, 'unknown period'), v.age_range, v.gender,
                v.demo_start, v.demo_end, v.demo_status
        ORDER BY v.demo_start
        """;

    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString(1, email);
    ResultSet rs = ps.executeQuery();

    System.out.println("\n--- Customer Sales by Demographic Period: " + email + " ---");
        
    System.out.printf("%-20s %-12s %-10s %-14s %-14s %-12s %-12s %-12s %-15s%n",
                "City", "Age Range", "Gender", "Demo Start",
                "Demo End", "Status", "Num Orders", "Total Spent", "Avg Order Value");
        System.out.println("-".repeat(125));
 
        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            System.out.printf("%-20s %-12s %-10s %-14s %-14s %-12s %-12d %-12s %-15s%n",
                    rs.getString("demo_city"),
                    rs.getString("age_range"),
                    rs.getString("gender"),
                    rs.getString("demo_start"),
                    rs.getString("demo_end") != null ? rs.getString("demo_end") : "current",
                    rs.getString("demo_status"),
                    rs.getInt("num_orders"),
                    rs.getString("total_spent"),
                    rs.getString("avg_order_value"));
        }
 
        if (!hasResults) {
            System.out.println("No results found for: " + email);
        }

rs.close();
ps.close();

}

//asks user for menu name to view monthly revenue trend
// Menu C — REQ7 aggregation + group by
public static void menuC(Connection conn) throws SQLException {
    String itemName = getUserInput("Enter menu item name to view monthly revenue trend: ");

    String sql = """
        SELECT
            DATE_FORMAT(o.order_timestamp, '%Y-%m') AS month,
            mi.item_name,
            COUNT(oi.id)                            AS num_line_items,
            SUM(oi.quantity)                        AS units_sold,
            SUM(oi.quantity * oi.item_price_at_order) AS monthly_revenue,
            ROUND(AVG(oi.item_price_at_order), 2)   AS avg_price_charged
        FROM order_item oi
        JOIN orders    o  ON oi.order_id    = o.id
        JOIN menu_item mi ON oi.menu_item_id = mi.id
        WHERE mi.item_name = ?
        GROUP BY DATE_FORMAT(o.order_timestamp, '%Y-%m'), mi.item_name
        ORDER BY month
        """;

    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString(1, itemName);
    ResultSet rs = ps.executeQuery();

    System.out.println("\n--- Monthly Revenue Trend: " + itemName + " ---");
    System.out.printf("%-10s %-20s %-15s %-12s %-18s %-18s%n",
        "Month", "Item Name", "Num Line Items",
        "Units Sold", "Monthly Revenue", "Avg Price Charged");
    System.out.println("-".repeat(95));

    boolean hasResults = false;
    while (rs.next()) {
        hasResults = true;
        System.out.printf("%-10s %-20s %-15d %-12d %-18s %-18s%n",
            rs.getString("month"),
            rs.getString("item_name"),
            rs.getInt("num_line_items"),
            rs.getInt("units_sold"),
            rs.getString("monthly_revenue"),
            rs.getString("avg_price_charged"));
    }

    if (!hasResults) {
        System.out.println("No results found for: " + itemName);
    }

    rs.close();
    ps.close();
}

//track customer performance by age range (use for REQ14)
// Menu D — REQ7 aggregation + group by
public static void menuD(Connection conn) throws SQLException {
    String ageRange = getUserInput("Enter age range to analyze (e.g. 20-29): ");

    String sql = """
        SELECT
            v.age_range,
            v.demo_city,
            v.demo_status,
            COUNT(DISTINCT v.customer_id)        AS unique_customers,
            COUNT(v.order_id)                    AS total_orders,
            SUM(v.final_total)                   AS total_revenue,
            ROUND(AVG(v.final_total), 2)         AS avg_order_value,
            MAX(v.final_total)                   AS largest_order
        FROM v_customer_sales_by_demo v
        WHERE v.age_range = ?
        GROUP BY v.age_range, v.demo_city, v.demo_status
        ORDER BY total_revenue DESC
        """;

    PreparedStatement ps = conn.prepareStatement(sql);
    ps.setString(1, ageRange);
    ResultSet rs = ps.executeQuery();

    System.out.println("\n--- Customer Performance by Age Range: " + ageRange + " ---");
        
    System.out.printf("%-12s %-20s %-14s %-18s %-14s %-15s %-16s %-14s%n",
                "Age Range", "City", "Demo Status", "Unique Customers",
                "Total Orders", "Total Revenue", "Avg Order Value", "Largest Order");
    System.out.println("-".repeat(125));
 
    boolean hasResults = false;
    while (rs.next()) {
        hasResults = true;
        System.out.printf("%-12s %-20s %-14s %-18d %-14d %-15s %-16s %-14s%n",
                rs.getString("age_range"),
                rs.getString("demo_city"),
                rs.getString("demo_status"),
                rs.getInt("unique_customers"),
                rs.getInt("total_orders"),
                rs.getString("total_revenue"),
                rs.getString("avg_order_value"),
                rs.getString("largest_order"));
        }
 
        if (!hasResults) {
            System.out.println("No results found for age range: " + ageRange);
        }
 
        rs.close();
        ps.close();
    }
}
