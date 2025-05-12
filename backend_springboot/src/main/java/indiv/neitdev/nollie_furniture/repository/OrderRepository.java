package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Order;
import indiv.neitdev.nollie_furniture.entity.User;
import indiv.neitdev.nollie_furniture.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    /**
     * Find all orders for a specific user
     * @param user the user
     * @return list of orders
     */
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    /**
     * Search orders with filtering
     * @param user the user
     * @param orderId optional order ID to search for (null to ignore)
     * @param startDate optional start date (null to ignore)
     * @param endDate optional end date (null to ignore)
     * @param pageable pagination information
     * @return page of matching orders
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user " +
           "AND (:orderId IS NULL OR o.id = :orderId) " +
           "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.orderDate <= :endDate) ")
    Page<Order> searchOrders(
            @Param("user") User user,
            @Param("orderId") Integer orderId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
            
    /**
     * Enhanced search for orders with more flexible filtering options
     * @param user the user
     * @param searchTerm optional search term for ID, name, email, phone, address
     * @param startDate optional start date (null to ignore)
     * @param endDate optional end date (null to ignore)
     * @param paymentMethod optional payment method filter
     * @param status optional order status filter
     * @param pageable pagination information
     * @return page of matching orders
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user " +
           "AND (:searchTerm IS NULL OR " +
           "     CAST(o.id AS string) = :searchTerm OR " +
           "     LOWER(o.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(o.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(o.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(o.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
           "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<Order> enhancedSearchOrders(
            @Param("user") User user,
            @Param("searchTerm") String searchTerm,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("paymentMethod") String paymentMethod,
            @Param("status") OrderStatus status,
            Pageable pageable);
}
