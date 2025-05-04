package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}
