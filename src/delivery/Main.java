package delivery;

import delivery.menu.AdminMenu;
import delivery.menu.AnalysisMenu;
import delivery.menu.CustomerMenu;
import delivery.menu.OrderMenu;
import delivery.util.ConsoleUI;

import java.util.Scanner;

/**
 * Application entry: main menu loop and routing to feature menus.
 */
public class Main {

    public static void main(String[] args) {
        boolean running = true;

        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                ConsoleUI.printBanner();
                ConsoleUI.printMainMenu();
                System.out.print("Enter choice: ");

                if (!scanner.hasNextInt()) {
                    scanner.nextLine();
                    System.out.println(ConsoleUI.INVALID_CHOICE_MESSAGE);
                    System.out.println();
                    continue;
                }

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> CustomerMenu.insertCustomer();
                    case 2 -> OrderMenu.insertOrder();
                    case 3 -> OrderMenu.searchCustomerOrders();
                    case 4 -> AnalysisMenu.searchRestaurantSales();
                    case 5 -> AnalysisMenu.priceChangeAnalysis();
                    case 6 -> AnalysisMenu.customerDemographicsAnalysis();
                    case 7 -> CustomerMenu.updateCustomer();
                    case 8 -> AdminMenu.updateMenuPrice();
                    case 9 -> CustomerMenu.deleteCustomer();
                    case 10 -> OrderMenu.deleteOrder();
                    case 0 -> {
                        System.out.println("Goodbye.");
                        running = false;
                    }
                    default -> System.out.println(ConsoleUI.INVALID_CHOICE_MESSAGE);
                }

                if (running) {
                    System.out.println();
                }
            }
        }
    }
}
