package delivery.menu;

import delivery.service.AnalysisService;

public final class AnalysisMenu {

    private AnalysisMenu() {
    }

    public static void searchRestaurantSales() {
        AnalysisService.searchRestaurantSales();
    }

    public static void priceChangeAnalysis() {
        AnalysisService.priceChangeAnalysis();
    }

    public static void customerDemographicsAnalysis() {
        AnalysisService.analyzeCustomerDemographics();
    }
}
