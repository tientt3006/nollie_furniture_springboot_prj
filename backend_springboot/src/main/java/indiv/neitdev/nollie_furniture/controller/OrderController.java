package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.MakeOrderRequest;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    OrderService orderService;

    @PostMapping("/make-order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<String> makeOrder(@RequestBody MakeOrderRequest request) {
        log.info("Cart add request received: {}", request);
        var result = orderService.makeOrder(request);
        return ApiResponse.<String>builder().result(result).build();
    }
}
