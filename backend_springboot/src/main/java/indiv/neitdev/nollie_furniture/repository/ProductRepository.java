package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByNameIgnoreCase(String name);
    
    // Find products by category ID
    @Query("SELECT p FROM Product p WHERE (:categoryName IS NULL OR p.category.name = :categoryName)")
    Page<Product> findByCategory(@Param("categoryName") String categoryName, Pageable pageable);
    
    // Find products by category ID and search term (ID or name)
    @Query("SELECT p FROM Product p WHERE " +
           "(:categoryName IS NULL OR p.category.name = :categoryName) AND " +
           "(:search IS NULL OR CAST(p.id AS string) = :search OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findByCategoryAndSearch(
            @Param("categoryName") String categoryName,
            @Param("search") String search,
            Pageable pageable);
}
