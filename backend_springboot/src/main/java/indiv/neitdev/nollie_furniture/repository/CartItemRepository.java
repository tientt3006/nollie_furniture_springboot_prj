package indiv.neitdev.nollie_furniture.repository;

import indiv.neitdev.nollie_furniture.entity.Cart;
import indiv.neitdev.nollie_furniture.entity.CartItem;
import indiv.neitdev.nollie_furniture.entity.Product;
import indiv.neitdev.nollie_furniture.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCart(Cart cart);
    
    // Find cart item with base product (no option selected)
    Optional<CartItem> findByCartAndProductAndProductOptionValueIsNull(Cart cart, Product product);
    
    // Find cart item with specific product option value
    Optional<CartItem> findByCartAndProductAndProductOptionValue(Cart cart, Product product, ProductOptionValue productOptionValue);
}
