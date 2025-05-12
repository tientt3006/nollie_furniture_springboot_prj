package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.response.OrderResponse;
import indiv.neitdev.nollie_furniture.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
