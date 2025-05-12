package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>{
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
@Query(value = """
       SELECT DISTINCT p.* FROM products p
       LEFT JOIN categories c ON p.category_id = c.id
       LEFT JOIN products_options po ON p.id = po.product_id
       LEFT JOIN products_options_value pov ON po.id = pov.product_option_id
       LEFT JOIN options_value ov ON pov.option_value_id = ov.id
       LEFT JOIN options o ON ov.option_id = o.id
       WHERE (:search IS NULL 
              OR CONVERT(varchar, p.id) LIKE :search
              OR p.name LIKE :search)
         AND (:categoryIds IS NULL OR p.category_id IN (:categoryIds))
         AND (:colorIds IS NULL 
              OR EXISTS (
                  SELECT 1 FROM products_options_value pov2 
                  JOIN options_value ov2 ON pov2.option_value_id = ov2.id
                  JOIN products_options po2 ON pov2.product_option_id = po2.id
                  JOIN options o2 ON ov2.option_id = o2.id
                  WHERE po2.product_id = p.id 
                    AND o2.name = 'Color'
                    AND ov2.id IN (:colorIds)
              ))
         AND (:materialIds IS NULL 
              OR EXISTS (
                  SELECT 1 FROM products_options_value pov3 
                  JOIN options_value ov3 ON pov3.option_value_id = ov3.id
                  JOIN products_options po3 ON pov3.product_option_id = po3.id
                  JOIN options o3 ON ov3.option_id = o3.id
                  WHERE po3.product_id = p.id 
                    AND o3.name = 'Material'
                    AND ov3.id IN (:materialIds)
              ))
         AND (:minPrice IS NULL OR p.base_price >= :minPrice)
         AND (:maxPrice IS NULL OR p.base_price <= :maxPrice)
         AND (:minHeight IS NULL OR p.height >= :minHeight)
         AND (:maxHeight IS NULL OR p.height <= :maxHeight)
         AND (:minWidth IS NULL OR p.width >= :minWidth)
         AND (:maxWidth IS NULL OR p.width <= :maxWidth)
         AND (:minLength IS NULL OR p.length >= :minLength)
         AND (:maxLength IS NULL OR p.length <= :maxLength)
       """,
                countQuery = """
       SELECT COUNT(DISTINCT p.id) FROM products p
       LEFT JOIN categories c ON p.category_id = c.id
       LEFT JOIN products_options po ON p.id = po.product_id
       LEFT JOIN products_options_value pov ON po.id = pov.product_option_id
       LEFT JOIN options_value ov ON pov.option_value_id = ov.id
       LEFT JOIN options o ON ov.option_id = o.id
       WHERE (:search IS NULL 
              OR CONVERT(varchar, p.id) LIKE :search
              OR p.name LIKE :search)
         AND (:categoryIds IS NULL OR p.category_id IN (:categoryIds))
         AND (:colorIds IS NULL 
              OR EXISTS (
                  SELECT 1 FROM products_options_value pov2 
                  JOIN options_value ov2 ON pov2.option_value_id = ov2.id
                  JOIN products_options po2 ON pov2.product_option_id = po2.id
                  JOIN options o2 ON ov2.option_id = o2.id
                  WHERE po2.product_id = p.id 
                    AND o2.name = 'Color'
                    AND ov2.id IN (:colorIds)
              ))
         AND (:materialIds IS NULL 
              OR EXISTS (
                  SELECT 1 FROM products_options_value pov3 
                  JOIN options_value ov3 ON pov3.option_value_id = ov3.id
                  JOIN products_options po3 ON pov3.product_option_id = po3.id
                  JOIN options o3 ON ov3.option_id = o3.id
                  WHERE po3.product_id = p.id 
                    AND o3.name = 'Material'
                    AND ov3.id IN (:materialIds)
              ))
         AND (:minPrice IS NULL OR p.base_price >= :minPrice)
         AND (:maxPrice IS NULL OR p.base_price <= :maxPrice)
         AND (:minHeight IS NULL OR p.height >= :minHeight)
         AND (:maxHeight IS NULL OR p.height <= :maxHeight)
         AND (:minWidth IS NULL OR p.width >= :minWidth)
         AND (:maxWidth IS NULL OR p.width <= :maxWidth)
         AND (:minLength IS NULL OR p.length >= :minLength)
         AND (:maxLength IS NULL OR p.length <= :maxLength)
       """,
                nativeQuery = true)
        Page<Product> findFilteredProducts(
                @Param("search") String search,
                @Param("categoryIds") List<Integer> categoryIds,
                @Param("colorIds") List<Integer> colorIds,
                @Param("materialIds") List<Integer> materialIds,
                @Param("minPrice") BigDecimal minPrice,
                @Param("maxPrice") BigDecimal maxPrice,
                @Param("minHeight") BigDecimal minHeight,
                @Param("maxHeight") BigDecimal maxHeight,
                @Param("minWidth") BigDecimal minWidth,
                @Param("maxWidth") BigDecimal maxWidth,
                @Param("minLength") BigDecimal minLength,
                @Param("maxLength") BigDecimal maxLength,
                Pageable pageable);


}
