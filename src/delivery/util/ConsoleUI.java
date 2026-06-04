package delivery.util;

/**
 * Shared console formatting and input validation messages.
 */
public final class ConsoleUI {

    private static final String BANNER = """
            =================================
                  DELIVERY APPLICATION
            =================================
            """;

    private static final String MENU = """
            Delivery System

            1. Insert Customer
            2. Insert Order

            3. Search Customer Orders
            4. Search Restaurant Sales
            5. Analyze Price Change
            6. Analyze Customer Demographics

            7. Update Customer
            8. Update Menu Price

            9. Delete Customer
            10. Delete Order

            0. Exit
            """;

    public static final String INVALID_CHOICE_MESSAGE =
            "Please enter a number between 0 and 10.";

    private ConsoleUI() {
    }

    public static void printBanner() {
        System.out.println(BANNER);
    }

    public static void printMainMenu() {
        System.out.println(MENU);
    }
}
