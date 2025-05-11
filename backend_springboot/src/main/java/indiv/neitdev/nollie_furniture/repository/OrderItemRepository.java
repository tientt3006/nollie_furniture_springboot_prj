package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.OrderItem;
import indiv.neitdev.nollie_furniture.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    /**
     * Find top selling products in the last month
     * @param startDate the start date to consider orders from
     * @param limit the maximum number of products to return
     * @return list of products sorted by total quantity sold in descending order
     */
    @Query("SELECT oi.product FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.orderDate >= :startDate " +
           "AND oi.product IS NOT NULL " +
           "GROUP BY oi.product " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Product> findTopSellingProducts(@Param("startDate") LocalDateTime startDate, @Param("limit") int limit);
}
