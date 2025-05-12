package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.dto.response.OrderResponse;
import indiv.neitdev.nollie_furniture.entity.Product;

import java.util.List;
import java.util.Map;

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
    Map<String, Integer> makeOrder(MakeOrderRequest request);
    
    /**
     * Get order details by order ID
     * @param orderId the order ID
     * @return the order response with details
     */
    OrderResponse getOrderById(Integer orderId);
}
