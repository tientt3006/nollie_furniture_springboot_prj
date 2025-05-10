package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.OptionValue;
import indiv.neitdev.nollie_furniture.entity.ProductOption;
import indiv.neitdev.nollie_furniture.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {
    List<ProductOptionValue> findByOptionValue(OptionValue optionValue);

    List<ProductOptionValue> findByProductOption(ProductOption productOption);
}
