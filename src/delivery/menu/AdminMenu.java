package delivery.menu;

import delivery.service.AdminService;

public final class AdminMenu {

    private AdminMenu() {
    }

    public static void updateMenuPrice() {
        AdminService.updateMenuPrice(); 
    }
}