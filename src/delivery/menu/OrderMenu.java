package delivery.menu;

import delivery.service.OrderService;

public final class OrderMenu {

    private OrderMenu() {
    }

    public static void insertOrder() {
        System.out.println("Insert Order - Coming Soon");
    }

    public static void deleteOrder() {
        OrderService.deleteOrder(); 
    }

    public static void searchCustomerOrders() {
        System.out.println("Search Customer Orders - Coming Soon");
    }
}
