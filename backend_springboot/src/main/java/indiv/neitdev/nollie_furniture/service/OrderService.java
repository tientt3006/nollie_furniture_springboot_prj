package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.dto.response.OrderResponse;
import indiv.neitdev.nollie_furniture.dto.response.OrderSummaryResponse;
import indiv.neitdev.nollie_furniture.entity.Product;
import indiv.neitdev.nollie_furniture.enums.OrderStatus;
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
    
    /**
     * Enhanced search for orders with more flexible filtering options
     * @param pageable pagination information
     * @param searchTerm optional search term for ID, name, email, phone, address
     * @param startDate optional start date for filtering
     * @param endDate optional end date for filtering
     * @param paymentMethod optional payment method filter
     * @param status optional order status filter
     * @return page of order summaries matching the criteria
     */
    Page<OrderSummaryResponse> enhancedSearchUserOrders(
            Pageable pageable,
            String searchTerm,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String paymentMethod,
            OrderStatus status);
    
    /**
     * Cancel an order with refund rules:
     * - If within 3 days of order creation and status is not ON_DELIVERY: 100% refund
     * - If status is ON_DELIVERY: 0% refund
     * - Cannot cancel if status is RECEIVED or already CANCELED
     * 
     * @param orderId the ID of the order to cancel
     * @return response containing cancellation details
     */
    Map<String, Object> cancelOrder(Integer orderId);
}
