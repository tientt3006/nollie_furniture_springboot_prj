package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.dto.response.OrderItemResponse;
import indiv.neitdev.nollie_furniture.dto.response.OrderResponse;
import indiv.neitdev.nollie_furniture.dto.response.OrderSummaryResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    @Transactional
    public Map<String, Object> reorder(Integer orderId) {
        try {
            log.info("Reordering from order ID: {}", orderId);
            
            // 1. Get current authenticated user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Find the original order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            
            // 3. Verify that the order belongs to the current user
            if (!order.getUser().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            
            // 4. Get or create the user's cart
            Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = Cart.builder()
                                .user(user)
                                .total(BigDecimal.ZERO)
                                .build();
                        return cartRepository.save(newCart);
                    });
            
            // 5. Clear the current cart
            List<CartItem> currentCartItems = cartItemRepository.findByCart(cart);
            if (!currentCartItems.isEmpty()) {
                cartItemRepository.deleteAll(currentCartItems);
                cart.setTotal(BigDecimal.ZERO);
                cart = cartRepository.save(cart);
            }
            
            // 6. Get original order items
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            
            // Track unavailable items and total added items
            List<String> unavailableItems = new ArrayList<>();
            int totalItemsAdded = 0;
            
            // 7. Process each order item
            for (OrderItem orderItem : orderItems) {
                Product product = orderItem.getProduct();
                ProductOptionValue productOptionValue = orderItem.getProductOptionValue();
                int quantity = orderItem.getQuantity();
                
                // 8. Check if product still exists
                if (product == null || !productRepository.existsById(product.getId())) {
                    unavailableItems.add("Product no longer exists");
                    continue;
                }
                
                // 9. Refresh product data from database
                product = productRepository.findById(product.getId()).orElse(null);
                if (product == null) {
                    unavailableItems.add("Product no longer exists");
                    continue;
                }
                
                // 10. If item has option value, check if it still exists
                if (productOptionValue != null) {
                    try {
                        productOptionValue = productOptionValueRepository.findById((long) productOptionValue.getId())
                            .orElse(null);
                        
                        if (productOptionValue == null) {
                            unavailableItems.add("Product option " + product.getName() + " is no longer available");
                            continue;
                        }
                        
                        // 11. Check if sufficient quantity is available
                        if (productOptionValue.getQuantity() < quantity) {
                            // Add with available quantity instead
                            if (productOptionValue.getQuantity() > 0) {
                                addToCartWithOption(cart, product, productOptionValue, productOptionValue.getQuantity());
                                unavailableItems.add("Added " + productOptionValue.getQuantity() + " of " + 
                                    product.getName() + " (requested: " + quantity + ")");
                                totalItemsAdded++;
                            } else {
                                unavailableItems.add("Product " + product.getName() + " is out of stock");
                            }
                            continue;
                        }
                        
                        // 12. Add item with option to cart
                        addToCartWithOption(cart, product, productOptionValue, quantity);
                        totalItemsAdded++;
                        
                    } catch (Exception e) {
                        log.error("Error processing product option value: {}", e.getMessage());
                        unavailableItems.add("Error processing " + product.getName() + " options");
                        continue;
                    }
                } else {
                    // 13. Handle base product without options
                    // Check if sufficient quantity is available
                    if (product.getBaseProductQuantity() < quantity) {
                        // Add with available quantity instead
                        if (product.getBaseProductQuantity() > 0) {
                            addToCartBaseProduct(cart, product, product.getBaseProductQuantity());
                            unavailableItems.add("Added " + product.getBaseProductQuantity() + " of " + 
                                product.getName() + " (requested: " + quantity + ")");
                            totalItemsAdded++;
                        } else {
                            unavailableItems.add("Product " + product.getName() + " is out of stock");
                        }
                        continue;
                    }
                    
                    // 14. Add base product to cart
                    addToCartBaseProduct(cart, product, quantity);
                    totalItemsAdded++;
                }
            }
            
            // 15. Prepare response with status and warnings
            Map<String, Object> response = new HashMap<>();
            
            if (totalItemsAdded == 0) {
                response.put("status", "No items could be added to your cart");
            } else if (!unavailableItems.isEmpty()) {
                response.put("status", "Some items were added to your cart with modifications");
                response.put("warnings", unavailableItems);
            } else {
                response.put("status", "All items from your previous order have been added to your cart");
            }
            
            return response;
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during reorder: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    
    // Helper method to add a base product to cart
    private void addToCartBaseProduct(Cart cart, Product product, int quantity) {
        // Calculate item price
        BigDecimal itemPrice = product.getBasePrice();
        if (itemPrice == null) {
            itemPrice = BigDecimal.ZERO;
            log.warn("Product {} has null base price, using 0", product.getId());
        }
        
        BigDecimal totalItemPrice = itemPrice.multiply(BigDecimal.valueOf(quantity));
        
        // Create cart item
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .itemPrice(itemPrice)
                .build();
        
        cartItemRepository.save(cartItem);
        
        // Update cart total
        cart.setTotal(cart.getTotal().add(totalItemPrice));
        cartRepository.save(cart);
    }
    
    // Helper method to add a product with option value to cart
    private void addToCartWithOption(Cart cart, Product product, ProductOptionValue productOptionValue, int quantity) {
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
        
        // Create cart item with option value
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .productOptionValue(productOptionValue)
                .quantity(quantity)
                .itemPrice(itemPrice)
                .build();
        
        cartItemRepository.save(cartItem);
        
        // Update cart total
        cart.setTotal(cart.getTotal().add(totalItemPrice));
        cartRepository.save(cart);
    }

    @Override
    public List<OrderSummaryResponse> getAllUserOrders() {
        try {
            // 1. Get current authenticated user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Get all orders for user, sorted by date descending (newest first)
            List<Order> userOrders = orderRepository.findByUserOrderByOrderDateDesc(user);
            
            // 3. Map orders to DTOs
            List<OrderSummaryResponse> orderSummaries = new ArrayList<>();
            
            for (Order order : userOrders) {
                orderSummaries.add(mapOrderToSummary(order));
            }
            
            return orderSummaries;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving user orders: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public Page<OrderSummaryResponse> searchUserOrders(
            Pageable pageable,
            Integer orderId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        try {
            // 1. Get current authenticated user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Search orders with filters
            Page<Order> ordersPage = orderRepository.searchOrders(
                    user, orderId, startDate, endDate, pageable);
            
            // 3. Map to DTOs
            return ordersPage.map(this::mapOrderToSummary);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error searching user orders: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public Page<OrderSummaryResponse> enhancedSearchUserOrders(
            Pageable pageable,
            String searchTerm,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String paymentMethod,
            OrderStatus status) {
        try {
            // 1. Get current authenticated user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Clean up search term if provided
            String cleanSearchTerm = searchTerm;
            if (searchTerm != null && searchTerm.trim().isEmpty()) {
                cleanSearchTerm = null;
            }
            
            // 3. Search orders with enhanced filters
            Page<Order> ordersPage = orderRepository.enhancedSearchOrders(
                    user, cleanSearchTerm, startDate, endDate, paymentMethod, status, pageable);
            
            // 4. Map to DTOs
            return ordersPage.map(this::mapOrderToSummary);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error searching user orders: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public Page<OrderSummaryResponse> adminSearchOrders(
            Pageable pageable,
            Integer orderId,
            Integer userId,
            String searchText,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String paymentMethod,
            OrderStatus status,
            String sortBy,
            String sortDirection) {
        try {
            log.info("Admin searching orders with filters: orderId={}, userId={}, search={}, date range={} to {}, " +
                    "paymentMethod={}, status={}, sortBy={}, sortDir={}", 
                    orderId, userId, searchText, startDate, endDate, paymentMethod, status, sortBy, sortDirection);
            
            // Clean up search term if provided
            String cleanSearchText = null;
            if (searchText != null && !searchText.trim().isEmpty()) {
                cleanSearchText = searchText.trim();
            }
            
            // Create sort specification with default sorting by orderDate DESC if not specified
            Sort sort;
            
            if (sortBy != null && !sortBy.isEmpty()) {
                Sort.Direction direction = Sort.Direction.DESC; // Default direction
                
                if (sortDirection != null && sortDirection.equalsIgnoreCase("ASC")) {
                    direction = Sort.Direction.ASC;
                }
                
                // Validate sortBy field to prevent errors
                switch (sortBy.toLowerCase()) {
                    case "total":
                        sort = Sort.by(direction, "total");
                        break;
                    case "orderId":
                    case "id":
                        sort = Sort.by(direction, "id");
                        break;
                    case "fullName":
                    case "name":
                        sort = Sort.by(direction, "fullName");
                        break;
                    case "status":
                        sort = Sort.by(direction, "status");
                        break;
                    default:
                        // Default to orderDate
                        sort = Sort.by(Sort.Direction.DESC, "orderDate");
                }
            } else {
                // Default sorting: newest orders first
                sort = Sort.by(Sort.Direction.DESC, "orderDate");
            }
            
            // Create a new pageable with the sort specification
            Pageable pageableWithSort = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(),
                sort
            );
            
            // Execute the search
            Page<Order> ordersPage = orderRepository.adminSearchOrders(
                    orderId, 
                    userId,
                    cleanSearchText,
                    startDate, 
                    endDate,
                    paymentMethod, 
                    status,
                    pageableWithSort);
            
            // Map orders to DTOs
            return ordersPage.map(this::mapOrderToSummary);
            
        } catch (Exception e) {
            log.error("Error in admin order search: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Helper method to map an Order entity to OrderSummaryResponse
     */
    private OrderSummaryResponse mapOrderToSummary(Order order) {
        // Get the number of items and a thumbnail image (if available)
        Integer itemCount = 0;
        String thumbnailUrl = null;
        
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        itemCount = orderItems.size();
        
        // Get the first product's image as thumbnail
        if (!orderItems.isEmpty()) {
            OrderItem firstItem = orderItems.get(0);
            Product product = firstItem.getProduct();
            
            if (product != null) {
                List<ProductImg> productImages = productImgRepository.findByProduct(product);
                if (!productImages.isEmpty()) {
                    thumbnailUrl = productImages.get(0).getImgUrl();
                }
            }
        }
        
        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .total(order.getTotal())
                .status(order.getStatus())
                .statusDetail(order.getStatusDetail())
                .fullName(order.getFullName())
                .address(order.getAddress())
                .phone(order.getPhone())
                .paymentMethod(order.getPaymentMethod())
                .itemCount(itemCount)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    @Override
    @Transactional
    public Map<String, Object> cancelOrder(Integer orderId) {
        try {
            log.info("Processing order cancellation for order ID: {}", orderId);
            
            // 1. Get current authenticated user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            // 2. Find the order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            
            // 3. Verify that the order belongs to the current user
            if (!order.getUser().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            
            // 4. Check if order can be canceled
            if (order.getStatus() == OrderStatus.CANCELED) {
                throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED);
            }
            
            if (order.getStatus() == OrderStatus.RECEIVED) {
                throw new AppException(ErrorCode.ORDER_ALREADY_RECEIVED);
            }
            
            // 5. Determine refund percentage based on rules
            BigDecimal refundPercentage;
            String refundMessage;
            
            if (order.getStatus() == OrderStatus.ON_DELIVERY) {
                // No refund if already in delivery
                refundPercentage = BigDecimal.ZERO;
                refundMessage = "Order was in delivery, no refund provided.";
            } else {
                // Check if within 3 days of order creation
                LocalDateTime orderDate = order.getOrderDate();
                LocalDateTime thresholdDate = orderDate.plusDays(3);
                LocalDateTime now = LocalDateTime.now();
                
                if (now.isBefore(thresholdDate)) {
                    // Within 3 days, 100% refund
                    refundPercentage = BigDecimal.ONE;  // 1.0 = 100%
                    refundMessage = "Order canceled within 3 days, full refund will be processed.";
                } else {
                    // After 3 days but before delivery, still use the 0% refund rule (can be adjusted if needed)
                    refundPercentage = BigDecimal.ZERO;
                    refundMessage = "Order cancellation period exceeded, no refund provided.";
                }
            }
            
            // 6. Get order items and restore product quantities
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                if(product == null) {
                    continue; // Skip if product is null
                }
                ProductOptionValue optionValue = item.getProductOptionValue();
                int quantity = item.getQuantity();
                
                // Restore quantities back to inventory
                if (optionValue == null) {
                    // Restore base product quantity
                    product.setBaseProductQuantity(product.getBaseProductQuantity() + quantity);
                    productRepository.save(product);
                } else {
                    // Restore option value quantity
                    optionValue.setQuantity(optionValue.getQuantity() + quantity);
                    productOptionValueRepository.save(optionValue);
                }
            }
            
            // 7. Update order status and refund information
            order.setStatus(OrderStatus.CANCELED);
            order.setCancelDate(LocalDateTime.now());
            order.setRefund(refundPercentage);
            order.setStatusDetail("Canceled by customer. " + refundMessage);
            
            // 8. Save the updated order
            orderRepository.save(order);
            
            // 9. Build response with details
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Order canceled successfully");
            response.put("orderId", order.getId());
            response.put("refundPercentage", refundPercentage.multiply(new BigDecimal(100)).intValue() + "%");
            response.put("message", refundMessage);
            
            log.info("Order {} successfully canceled with {} refund", orderId, refundPercentage);
            return response;
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error canceling order: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.ORDER_CANCELLATION_FAILED);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> adminAdvanceOrderStatus(Integer orderId) {
        try {
            log.info("Admin advancing order status for order ID: {}", orderId);
            
            // Find the order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            
            // Check current status and determine next status
            OrderStatus currentStatus = order.getStatus();
            Map<String, Object> response = new HashMap<>();
            
            switch (currentStatus) {
                case ORDER_SUCCESSFUL:
                    // Update to ON_DELIVERY
                    order.setStatus(OrderStatus.ON_DELIVERY);
                    order.setStartDeliveryDate(LocalDateTime.now());
                    order.setStatusDetail("Order is now being delivered.");
                    orderRepository.save(order);
                    
                    response.put("status", "success");
                    response.put("message", "Order status updated from ORDER_SUCCESSFUL to ON_DELIVERY");
                    response.put("orderId", order.getId());
                    response.put("newStatus", OrderStatus.ON_DELIVERY.toString());
                    response.put("startDeliveryDate", order.getStartDeliveryDate());
                    break;
                    
                case ON_DELIVERY:
                    // Update to RECEIVED
                    order.setStatus(OrderStatus.RECEIVED);
                    order.setReceiveDate(LocalDateTime.now());
                    order.setRefund(BigDecimal.ZERO); // 0% refund for received orders
                    order.setStatusDetail("Order has been received by customer.");
                    orderRepository.save(order);
                    
                    response.put("status", "success");
                    response.put("message", "Order status updated from ON_DELIVERY to RECEIVED");
                    response.put("orderId", order.getId());
                    response.put("newStatus", OrderStatus.RECEIVED.toString());
                    response.put("receiveDate", order.getReceiveDate());
                    break;
                    
                case RECEIVED:
                    response.put("status", "unchanged");
                    response.put("message", "Order is already in RECEIVED status. No changes made.");
                    response.put("orderId", order.getId());
                    response.put("currentStatus", OrderStatus.RECEIVED.toString());
                    break;
                    
                case CANCELED:
                    response.put("status", "unchanged");
                    response.put("message", "Order is in CANCELED status and cannot be advanced. No changes made.");
                    response.put("orderId", order.getId());
                    response.put("currentStatus", OrderStatus.CANCELED.toString());
                    break;
            }
            
            log.info("Admin order status advancement completed for order ID {}: {}", orderId, response);
            return response;
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error advancing order status: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> adminCancelOrder(Integer orderId, String cancelReason) {
        try {
            log.info("Admin canceling order ID: {} with reason: {}", orderId, cancelReason);
            
            // Find the order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            
            // Check if order can be canceled
            if (order.getStatus() == OrderStatus.CANCELED) {
                throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED);
            }
            
            if (order.getStatus() == OrderStatus.RECEIVED) {
                throw new AppException(ErrorCode.ORDER_ALREADY_RECEIVED);
            }
            
            // Get order items to restore product quantities
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                if (product == null) {
                    continue; // Skip if product is null (unlikely but safe check)
                }
                ProductOptionValue optionValue = item.getProductOptionValue();
                int quantity = item.getQuantity();
                
                // Restore quantities back to inventory
                if (optionValue == null) {
                    // Restore base product quantity
                    product.setBaseProductQuantity(product.getBaseProductQuantity() + quantity);
                    productRepository.save(product);
                } else {
                    // Restore option value quantity
                    optionValue.setQuantity(optionValue.getQuantity() + quantity);
                    productOptionValueRepository.save(optionValue);
                }
            }
            
            // Set cancellation details based on current status
            String statusDetail;
            if (order.getStatus() == OrderStatus.ORDER_SUCCESSFUL) {
                statusDetail = "Order canceled by admin. We sincerely apologize for any inconvenience. " +
                         (cancelReason != null && !cancelReason.isEmpty() ? "Reason: " + cancelReason : "");
            } else {
                // ON_DELIVERY
                statusDetail = "Order canceled during delivery. Customer did not accept the delivery. " +
                         (cancelReason != null && !cancelReason.isEmpty() ? "Additional notes: " + cancelReason : "");
            }
            
            // Update order status and refund information
            order.setStatus(OrderStatus.CANCELED);
            order.setCancelDate(LocalDateTime.now());
            order.setRefund(BigDecimal.ONE); // 100% refund represented as 1.0
            order.setStatusDetail(statusDetail);
            
            // Save the updated order
            orderRepository.save(order);
            
            // Build response with details
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Order canceled successfully by admin");
            response.put("orderId", order.getId());
            response.put("cancelDate", order.getCancelDate());
            response.put("refund", "100%");
            response.put("statusDetail", statusDetail);
            
            log.info("Order {} successfully canceled by admin", orderId);
            return response;
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in admin order cancellation: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.ORDER_CANCELLATION_FAILED);
        }
    }

    @Override
    public OrderResponse getOrderDetailsByAdmin(Integer orderId) {
        try {
            // 1. Find the order by ID without user ownership check
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            
            // 2. Get order items
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            List<OrderItemResponse> orderItemResponses = new ArrayList<>();
            
            // 3. Convert order items to response objects
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                ProductOptionValue optionValue = item.getProductOptionValue();
                
                // Get product image for thumbnail
                String imageUrl = null;
                if (product != null) {
                    List<ProductImg> productImages = productImgRepository.findByProduct(product);
                    if (!productImages.isEmpty()) {
                        imageUrl = productImages.get(0).getImgUrl();
                    }
                }
                
                // Build response with option information if available
                OrderItemResponse orderItemResponse;
                if (optionValue != null) {
                    // Item has an option value
                    OptionValue value = optionValue.getOptionValue();
                    Option option = value.getOption();
                    
                    orderItemResponse = OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(product != null ? product.getId() : null)
                            .productName(product != null ? product.getName() : "Product no longer available")
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
                            .productId(product != null ? product.getId() : null)
                            .productName(product != null ? product.getName() : "Product no longer available")
                            .quantity(item.getQuantity())
                            .itemPrice(item.getItemPrice())
                            .totalPrice(item.getItemPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .productImageUrl(imageUrl)
                            .build();
                }
                
                orderItemResponses.add(orderItemResponse);
            }
            
            // 4. Build and return the complete order response
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
            log.error("Error getting order details by admin: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
