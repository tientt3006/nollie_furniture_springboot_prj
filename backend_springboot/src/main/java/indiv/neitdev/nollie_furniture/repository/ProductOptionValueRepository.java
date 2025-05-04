package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {
}
