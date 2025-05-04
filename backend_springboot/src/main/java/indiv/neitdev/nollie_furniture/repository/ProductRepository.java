package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}
