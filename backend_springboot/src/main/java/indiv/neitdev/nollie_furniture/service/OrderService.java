package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.entity.Product;

import java.util.List;

public interface OrderService {
    /**
     * Get top selling products in the last month
     * @param limit the number of products to return
     * @return list of top selling products
     */
    List<Product> getTopSellingProducts(int limit);
    
    /**
     * Create a new order from the customer's cart
     * @param request the order request containing customer information
     * @return success message
     */
    String makeOrder(MakeOrderRequest request);
}
