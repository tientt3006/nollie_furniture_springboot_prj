package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.response.OrderResponse;
import indiv.neitdev.nollie_furniture.dto.response.OrderSummaryResponse;
import indiv.neitdev.nollie_furniture.enums.OrderStatus;
import indiv.neitdev.nollie_furniture.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    OrderService orderService;

    @PostMapping("/make-order")
    @PreAuthorize("hasRole('CUSTOMER')")
    // return Map<String, Integer> to indicate orderstatus and order ID
    public ApiResponse<Map<String, Integer>> makeOrder(@RequestBody MakeOrderRequest request) {
        log.info("Order request received: {}", request);
        var result = orderService.makeOrder(request);
        return ApiResponse.<Map<String, Integer>>builder().result(result).build();
    }
    
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Integer orderId) {
        log.info("Get order details request received for order ID: {}", orderId);
        var result = orderService.getOrderById(orderId);
        return ApiResponse.<OrderResponse>builder().result(result).build();
    }
    
    @PostMapping("/{orderId}/reorder")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<Map<String, Object>> reorder(@PathVariable Integer orderId) {
        log.info("Reorder request received for order ID: {}", orderId);
        var result = orderService.reorder(orderId);
        return ApiResponse.<Map<String, Object>>builder().result(result).build();
    }
    
    /**
     * Get all orders for the current authenticated user
     * @return list of order summaries
     */
    @GetMapping("/user/all")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<List<OrderSummaryResponse>> getAllUserOrders() {
        log.info("Request to get all user orders received");
        var result = orderService.getAllUserOrders();
        return ApiResponse.<List<OrderSummaryResponse>>builder().result(result).build();
    }
    
    /**
     * Enhanced search for orders with more flexible filtering options
     * @param page page number (0-based)
     * @param size page size
     * @param search optional search term for orderId, name, email, phone, or address
     * @param startDate optional start date for filtering
     * @param endDate optional end date for filtering
     * @param paymentMethod optional payment method filter
     * @param status optional order status filter
     * @return page of order summaries matching the criteria
     */
    @GetMapping("/user/search")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<Page<OrderSummaryResponse>> searchUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) OrderStatus status) {
        
        log.info("Enhanced order search request: page={}, size={}, search={}, startDate={}, endDate={}, paymentMethod={}, status={}",
                page, size, search, startDate, endDate, paymentMethod, status);
        
        // Create pageable with sorting by order date descending (newest first)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        
        var result = orderService.enhancedSearchUserOrders(
                pageRequest, search, startDate, endDate, paymentMethod, status);
                
        return ApiResponse.<Page<OrderSummaryResponse>>builder().result(result).build();
    }
}
