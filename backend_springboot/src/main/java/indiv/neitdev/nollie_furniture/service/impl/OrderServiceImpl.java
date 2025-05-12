package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.dto.response.OrderItemResponse;
import indiv.neitdev.nollie_furniture.dto.response.OrderResponse;
import indiv.neitdev.nollie_furniture.entity.*;
import indiv.neitdev.nollie_furniture.enums.OrderStatus;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.repository.*;
import indiv.neitdev.nollie_furniture.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    UserRepository userRepository;
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;
    ProductOptionValueRepository productOptionValueRepository;
    ProductImgRepository productImgRepository;
    
    @Override
    public List<Product> getTopSellingProducts(int limit) {
        try {
            // Calculate the date one month ago from now
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            
            // Get top selling products from the repository
            return orderItemRepository.findTopSellingProducts(oneMonthAgo, limit);
        } catch (Exception e) {
            log.error("Error getting top selling products: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    @Override
    @Transactional
    public Map<String, Integer> makeOrder(MakeOrderRequest request) {
        try {
            log.info("Creating new order from request: {}", request);
            
            // 1. Validate request
            validateOrderRequest(request);
            
            // 2. Get current authenticated user from SecurityContext
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 3. Find user's cart
            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            
            // 4. Get cart items
            List<CartItem> cartItems = cartItemRepository.findByCart(cart);
            
            // Check if cart is empty
            if (cartItems.isEmpty()) {
                throw new AppException(ErrorCode.CART_IS_EMPTY);
            }
            
            // 5. Create new order
            Order order = Order.builder()
                    .user(user)
                    .orderDate(LocalDateTime.now())
                    .total(cart.getTotal())
                    .status(OrderStatus.ORDER_SUCCESSFUL)
                    .fullName(request.getFullName())
                    .address(request.getAddress())
                    .email(request.getEmail() != null && !request.getEmail().isEmpty() ? request.getEmail() : user.getEmail())
                    .phone(request.getPhone())
                    .paymentMethod(request.getPaymentMethod())
                    .statusDetail(request.getNotes()) // Store notes in statusDetail field if needed
                    .build();
            
            // 6. Save order to get ID
            order = orderRepository.save(order);
            
            // 7. Create order items and update product quantities
            for (CartItem cartItem : cartItems) {
                Product product = cartItem.getProduct();
                ProductOptionValue productOptionValue = cartItem.getProductOptionValue();
                int quantity = cartItem.getQuantity();
                
                // 8. Check product availability and update stock
                if (productOptionValue == null) {
                    // Base product
                    if (product.getBaseProductQuantity() < quantity) {
                        throw new AppException(ErrorCode.PRODUCT_QUANTITY_INSUFFICIENT);
                    }
                    
                    // Update base product quantity
                    product.setBaseProductQuantity(product.getBaseProductQuantity() - quantity);
                    productRepository.save(product);
                } else {
                    // Product with option value
                    if (productOptionValue.getQuantity() < quantity) {
                        throw new AppException(ErrorCode.PRODUCT_QUANTITY_INSUFFICIENT);
                    }
                    
                    // Update option value quantity
                    productOptionValue.setQuantity(productOptionValue.getQuantity() - quantity);
                    productOptionValueRepository.save(productOptionValue);
                }
                
                // 9. Create and save order item
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .productOptionValue(productOptionValue)
                        .quantity(quantity)
                        .itemPrice(cartItem.getItemPrice())
                        .build();
                
                orderItemRepository.save(orderItem);
            }
            
            // 10. Clear the user's cart after successful order
            cartItemRepository.deleteAll(cartItems);
            cart.setTotal(java.math.BigDecimal.ZERO);
            cartRepository.save(cart);
            
            log.info("Order created successfully with ID: {}", order.getId());
            return Map.of("Đặt hàng thành công.", order.getId());


        } catch (AppException e) {
            log.error("Error creating order: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating order: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    private void validateOrderRequest(MakeOrderRequest request) {
        // Validate required fields
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_FULLNAME_REQUIRED);
        }
        
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_ADDRESS_REQUIRED);
        }
        
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_PHONE_REQUIRED);
        }
        
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_PAYMENT_METHOD_REQUIRED);
        }
    }

    @Override
    public OrderResponse getOrderById(Integer orderId) {
        try {
            // 1. Get current authenticated user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Find the order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            
            // 3. Check that the order belongs to the current user
            if (!order.getUser().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            
            // 4. Get order items
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            List<OrderItemResponse> orderItemResponses = new ArrayList<>();
            
            // 5. Convert order items to response objects
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                ProductOptionValue optionValue = item.getProductOptionValue();
                
                // Get product image for thumbnail
                String imageUrl = null;
                List<ProductImg> productImages = productImgRepository.findByProduct(product);
                if (!productImages.isEmpty()) {
                    imageUrl = productImages.get(0).getImgUrl();
                }
                
                // Build response with option information if available
                OrderItemResponse orderItemResponse;
                if (optionValue != null) {
                    // Item has an option value
                    OptionValue value = optionValue.getOptionValue();
                    Option option = value.getOption();
                    
                    orderItemResponse = OrderItemResponse.builder()
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
                    orderItemResponse = OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(product.getId())
                            .productName(product.getName())
                            .quantity(item.getQuantity())
                            .itemPrice(item.getItemPrice())
                            .totalPrice(item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .productImageUrl(imageUrl)
                            .build();
                }
                
                orderItemResponses.add(orderItemResponse);
            }
            
            // 6. Build and return the complete order response
            return OrderResponse.builder()
                    .orderId(order.getId())
                    .orderDate(order.getOrderDate())
                    .cancelDate(order.getCancelDate())
                    .startDeliveryDate(order.getStartDeliveryDate())
                    .receiveDate(order.getReceiveDate())
                    .total(order.getTotal())
                    .status(order.getStatus())
                    .statusDetail(order.getStatusDetail())
                    .refund(order.getRefund())
                    .fullName(order.getFullName())
                    .address(order.getAddress())
                    .email(order.getEmail())
                    .phone(order.getPhone())
                    .paymentMethod(order.getPaymentMethod())
                    .items(orderItemResponses)
                    .build();
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting order details: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
