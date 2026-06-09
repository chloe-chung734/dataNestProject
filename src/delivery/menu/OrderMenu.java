package delivery.menu;

import delivery.service.OrderService;

public final class OrderMenu {

    private OrderMenu() {
    }

    public static void insertOrder() {
        OrderService.insertOrder();
    }

    public static void deleteOrder() {
        OrderService.deleteOrder(); 
    }

    public static void searchCustomerOrders() {
        OrderService.searchCustomerOrders();
    }
}
