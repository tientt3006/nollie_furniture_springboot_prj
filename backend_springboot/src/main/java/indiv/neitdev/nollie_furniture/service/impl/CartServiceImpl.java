package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.AddToCartReq;
import indiv.neitdev.nollie_furniture.dto.response.CartResponse;
import indiv.neitdev.nollie_furniture.entity.*;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.repository.*;
import indiv.neitdev.nollie_furniture.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartServiceImpl implements CartService {
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    ProductOptionValueRepository productOptionValueRepository;

    @Override
    @Transactional
    public String addToCart(AddToCartReq request) {
        log.info("Adding to cart: {}", request);
        
        // 1. Get current authenticated user from SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // 2. Find or create cart for this user
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .total(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(newCart);
                });
        
        // 3. Get product and validate it exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        
        // 4. Handle base product quantity vs option values with quantities
        if (request.getBaseProductQuantity() != null) {
            // Handle base product (no options)
            addBaseProductToCart(cart, product, request.getBaseProductQuantity());
        } else if (request.getProductOptionValueIdsAndQuantity() != null && !request.getProductOptionValueIdsAndQuantity().isEmpty()) {
            // Handle product with options
            addProductWithOptionsToCart(cart, product, request.getProductOptionValueIdsAndQuantity());
        } else {
            // Neither base product quantity nor option values provided
            throw new AppException(ErrorCode.CART_INVALID_REQUEST);
        }
        
        return "Product added to cart successfully";
    }

    @Override
    public CartResponse viewCart() {
        return null;
    }

    private void addBaseProductToCart(Cart cart, Product product, Integer quantity) {
        // Validate quantity
        if (quantity <= 0) {
            throw new AppException(ErrorCode.PRODUCT_QUANTITY_INVALID);
        }
        
        // Check if base product quantity is available
        if (product.getBaseProductQuantity() < quantity) {
            throw new AppException(ErrorCode.PRODUCT_QUANTITY_INSUFFICIENT);
        }
        
        // Calculate item price - ensure we're using the correct base price from product
        BigDecimal itemPrice = product.getBasePrice();
        if (itemPrice == null) {
            itemPrice = BigDecimal.ZERO;
            log.warn("Product {} has null base price, using 0", product.getId());
        }
        
        BigDecimal totalItemPrice = itemPrice.multiply(BigDecimal.valueOf(quantity));
        
        // Find existing cart item or create new one
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProductAndProductOptionValueIsNull(cart, product);
        
        BigDecimal priceChange;
        
        if (existingCartItem.isPresent()) {
            // Update existing cart item
            CartItem cartItem = existingCartItem.get();
            int oldQuantity = cartItem.getQuantity();
            cartItem.setQuantity(oldQuantity + quantity);
            // Ensure item price is set correctly
            cartItem.setItemPrice(itemPrice);
            priceChange = itemPrice.multiply(BigDecimal.valueOf(quantity));
            cartItemRepository.save(cartItem);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .itemPrice(itemPrice) // Explicitly set the item price
                    .build();
            
            priceChange = totalItemPrice;
            cartItemRepository.save(cartItem);
        }
        
        // Update cart total
        cart.setTotal(cart.getTotal().add(priceChange));
        cartRepository.save(cart);
    }
    
    private void addProductWithOptionsToCart(Cart cart, Product product, List<Map<Integer, Integer>> productOptionValueIdsAndQuantity) {
        BigDecimal totalPriceChange = BigDecimal.ZERO;
        
        // Process each option value and quantity pair
        for (Map<Integer, Integer> optionValueMap : productOptionValueIdsAndQuantity) {
            if (optionValueMap == null || optionValueMap.isEmpty()) {
                continue;
            }
            
            // Each map should contain exactly one entry (optionValueId -> quantity)
            Map.Entry<Integer, Integer> entry = optionValueMap.entrySet().iterator().next();
            Integer productOptionValueId = entry.getKey(); // Changed variable name for clarity
            Integer quantity = entry.getValue();
            
            // Validate quantity
            if (quantity <= 0) {
                throw new AppException(ErrorCode.PRODUCT_QUANTITY_INVALID);
            }
            
            // Get product option value
            ProductOptionValue productOptionValue = productOptionValueRepository.findById(productOptionValueId.longValue())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_OPTION_VALUE_NOT_FOUND));
            
            // Verify that the product option value belongs to this product
            ProductOption productOption = productOptionValue.getProductOption();
            if (!productOption.getProduct().getId().equals(product.getId())) {
                throw new AppException(ErrorCode.PRODUCT_OPTION_VALUE_NOT_BELONG_TO_PRODUCT);
            }
            
            // Check if quantity is available
            if (productOptionValue.getQuantity() < quantity) {
                throw new AppException(ErrorCode.PRODUCT_QUANTITY_INSUFFICIENT);
            }
            
            // Calculate item price with option value additional price
            BigDecimal itemPrice = product.getBasePrice();
            if (itemPrice == null) {
                itemPrice = BigDecimal.ZERO;
                log.warn("Product {} has null base price, using 0", product.getId());
            }
            
            if (productOptionValue.getAddPrice() != null) {
                itemPrice = itemPrice.add(productOptionValue.getAddPrice());
            }
            
            BigDecimal totalItemPrice = itemPrice.multiply(BigDecimal.valueOf(quantity));
            
            // Find existing cart item with this option value or create new one
            Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProductAndProductOptionValue(
                    cart, product, productOptionValue);
            
            BigDecimal priceChange;
            
            if (existingCartItem.isPresent()) {
                // Update existing cart item
                CartItem cartItem = existingCartItem.get();
                int oldQuantity = cartItem.getQuantity();
                cartItem.setQuantity(oldQuantity + quantity);
                // Ensure item price is set correctly
                cartItem.setItemPrice(itemPrice);
                priceChange = itemPrice.multiply(BigDecimal.valueOf(quantity));
                cartItemRepository.save(cartItem);
            } else {
                // Create new cart item
                CartItem cartItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .productOptionValue(productOptionValue)
                        .quantity(quantity)
                        .itemPrice(itemPrice) // Explicitly set the correct item price
                        .build();
                
                priceChange = totalItemPrice;
                cartItemRepository.save(cartItem);
            }
            
            totalPriceChange = totalPriceChange.add(priceChange);
        }
        
        // Update cart total
        cart.setTotal(cart.getTotal().add(totalPriceChange));
        cartRepository.save(cart);
    }
}
