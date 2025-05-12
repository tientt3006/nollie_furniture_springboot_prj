package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.AddToCartReq;
import indiv.neitdev.nollie_furniture.dto.response.CartResponse;
import indiv.neitdev.nollie_furniture.dto.response.CartItemResponse;
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
    ProductImgRepository productImgRepository;

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
        try {
            // Get current authenticated user from SecurityContext
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // Find cart for this user or return empty cart
            Optional<Cart> optionalCart = cartRepository.findByUser(user);
            
            if (optionalCart.isEmpty()) {
                // Return empty cart if user doesn't have one yet
                return CartResponse.builder()
                        .cartId(null)
                        .total(BigDecimal.ZERO)
                        .items(new ArrayList<>())
                        .build();
            }
            
            Cart cart = optionalCart.get();
            List<CartItem> cartItems = cartItemRepository.findByCart(cart);
            List<CartItemResponse> cartItemResponses = new ArrayList<>();
            
            // Map cart items to response objects
            for (CartItem item : cartItems) {
                Product product = item.getProduct();
                ProductOptionValue optionValue = item.getProductOptionValue();
                
                // Get product images for thumbnail
                String imageUrl = null;
                List<ProductImg> productImages = productImgRepository.findByProduct(product);
                if (!productImages.isEmpty()) {
                    imageUrl = productImages.get(0).getImgUrl();
                }
                
                // Build response with option information if available
                CartItemResponse cartItemResponse;
                if (optionValue != null) {
                    // Item has an option value
                    OptionValue value = optionValue.getOptionValue();
                    Option option = value.getOption();
                    
                    cartItemResponse = CartItemResponse.builder()
                            .id(item.getId())
                            .productId(product.getId())
                            .productName(product.getName())
                            .quantity(item.getQuantity())
                            .itemPrice(item.getItemPrice())
                            .totalPrice(item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .productOptionValueId(optionValue.getId())
                            .optionName(option.getName())
                            .optionValueName(value.getValue())
                            .productImageUrl(imageUrl)
                            .build();
                } else {
                    // Base product with no options
                    cartItemResponse = CartItemResponse.builder()
                            .id(item.getId())
                            .productId(product.getId())
                            .productName(product.getName())
                            .quantity(item.getQuantity())
                            .itemPrice(item.getItemPrice())
                            .totalPrice(item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .productImageUrl(imageUrl)
                            .build();
                }
                
                cartItemResponses.add(cartItemResponse);
            }
            
            // Build and return the complete cart response
            return CartResponse.builder()
                    .cartId(cart.getId())
                    .total(cart.getTotal())
                    .items(cartItemResponses)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error viewing cart: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public String updateCartItemQuantity(Integer cartItemId, Integer quantity) {
        try {
            // Validate quantity
            if (quantity <= 0) {
                throw new AppException(ErrorCode.PRODUCT_QUANTITY_INVALID);
            }
            
            // 1. Get current authenticated user from SecurityContext
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Find the user's cart
            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            
            // 3. Find the cart item
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
            
            // 4. Verify the cart item belongs to the user's cart
            if (!cartItem.getCart().getId().equals(cart.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            
            // 5. Check product availability
            int oldQuantity = cartItem.getQuantity();
            Product product = cartItem.getProduct();
            ProductOptionValue optionValue = cartItem.getProductOptionValue();
            
            // 5a. Check if sufficient quantity available - for base product or option value
            if (optionValue == null) {
                // Base product quantity check
                if (product.getBaseProductQuantity() < quantity) {
                    throw new AppException(ErrorCode.PRODUCT_QUANTITY_INSUFFICIENT);
                }
            } else {
                // Option value quantity check
                if (optionValue.getQuantity() < quantity) {
                    throw new AppException(ErrorCode.PRODUCT_QUANTITY_INSUFFICIENT);
                }
            }
            
            // 6. Calculate price difference for cart total update
            BigDecimal oldTotalPrice = cartItem.getItemPrice().multiply(BigDecimal.valueOf(oldQuantity));
            BigDecimal newTotalPrice = cartItem.getItemPrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal priceDifference = newTotalPrice.subtract(oldTotalPrice);
            
            // 7. Update cart item quantity
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            
            // 8. Update cart total
            cart.setTotal(cart.getTotal().add(priceDifference));
            cartRepository.save(cart);
            
            return "Cart item quantity updated successfully";
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating cart item quantity: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public String removeCartItem(Integer cartItemId) {
        try {
            // 1. Get current authenticated user from SecurityContext
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Find the user's cart
            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            
            // 3. Find the cart item
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
            
            // 4. Verify the cart item belongs to the user's cart
            if (!cartItem.getCart().getId().equals(cart.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            
            // 5. Calculate price to subtract from cart total
            BigDecimal priceToSubtract = cartItem.getItemPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            
            // 6. Update cart total
            cart.setTotal(cart.getTotal().subtract(priceToSubtract));
            cartRepository.save(cart);
            
            // 7. Remove the cart item
            cartItemRepository.delete(cartItem);
            
            return "Cart item removed successfully";
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error removing cart item: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public String clearCart() {
        try {
            // 1. Get current authenticated user from SecurityContext
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Find the user's cart
            Optional<Cart> optionalCart = cartRepository.findByUser(user);
            
            // If cart doesn't exist or is already empty, just return success
            if (optionalCart.isEmpty()) {
                return "Cart is already empty";
            }
            
            Cart cart = optionalCart.get();
            
            // 3. Get all cart items
            List<CartItem> cartItems = cartItemRepository.findByCart(cart);
            
            // If no items, just return success
            if (cartItems.isEmpty()) {
                return "Cart is already empty";
            }
            
            // 4. Delete all cart items
            cartItemRepository.deleteAll(cartItems);
            
            // 5. Reset cart total to zero
            cart.setTotal(BigDecimal.ZERO);
            cartRepository.save(cart);
            
            return "All items removed from cart successfully";
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
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
