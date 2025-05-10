package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Option;
import indiv.neitdev.nollie_furniture.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Integer> {
    List<ProductOption> findByOption(Option option);
}
