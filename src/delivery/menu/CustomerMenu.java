package delivery.menu;

import delivery.service.CustomerService;

public final class CustomerMenu {

    private CustomerMenu() {
    }

    public static void insertCustomer() {
        CustomerService.insertCustomer();
    }

    public static void updateCustomer() {
        CustomerService.updateCustomer();  
    }

    public static void deleteCustomer() {
        CustomerService.deleteCustomer();  
    }
}
