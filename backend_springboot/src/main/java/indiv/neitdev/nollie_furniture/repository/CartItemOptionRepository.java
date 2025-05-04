package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.CartItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemOptionRepository extends JpaRepository<CartItemOption, Integer> {
}
