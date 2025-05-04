package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}
