package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
}
