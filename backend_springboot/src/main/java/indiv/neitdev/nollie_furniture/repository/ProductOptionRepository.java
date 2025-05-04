package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Integer> {
}
