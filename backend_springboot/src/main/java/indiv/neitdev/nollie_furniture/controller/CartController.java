package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.AddToCartReq;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.response.CartResponse;
import indiv.neitdev.nollie_furniture.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartController {
    CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<String> addToCart(@RequestBody AddToCartReq request) {
        log.info("Cart add request received: {}", request);
        var result = cartService.addToCart(request);
        return ApiResponse.<String>builder().result(result).build();
    }
    
    @GetMapping("/view")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<CartResponse> viewCart() {
        var result = cartService.viewCart();
        return ApiResponse.<CartResponse>builder().result(result).build();
    }
}
