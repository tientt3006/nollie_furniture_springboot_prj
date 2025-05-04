package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImgRepository extends JpaRepository<ProductImg, Integer> {
}
