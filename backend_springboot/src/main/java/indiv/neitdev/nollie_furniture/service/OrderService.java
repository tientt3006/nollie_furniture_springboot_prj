package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.dto.response.OrderResponse;
import indiv.neitdev.nollie_furniture.dto.response.OrderSummaryResponse;
import indiv.neitdev.nollie_furniture.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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
    
    /**
     * Reorder an existing order by copying its items to the user's cart
     * @param orderId the ID of the order to reorder
     * @return message indicating success or warnings about items that couldn't be added
     */
    Map<String, Object> reorder(Integer orderId);
    
    /**
     * Get all orders for the current authenticated user
     * @return list of order summaries
     */
    List<OrderSummaryResponse> getAllUserOrders();
    
    /**
     * Search orders with pagination and filtering
     * @param pageable pagination information
     * @param orderId optional order ID to search for
     * @param startDate optional start date for filtering
     * @param endDate optional end date for filtering
     * @return page of order summaries matching the criteria
     */
    Page<OrderSummaryResponse> searchUserOrders(
            Pageable pageable,
            Integer orderId,
            LocalDateTime startDate,
            LocalDateTime endDate);
}
