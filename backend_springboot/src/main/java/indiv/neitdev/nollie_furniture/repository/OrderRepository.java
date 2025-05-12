package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Order;
import indiv.neitdev.nollie_furniture.entity.User;
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
}
