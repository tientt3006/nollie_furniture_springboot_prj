package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.OptionCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.ProductCreateRequest;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.response.OptionResponse;
import indiv.neitdev.nollie_furniture.dto.response.ProductResponse;
import indiv.neitdev.nollie_furniture.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    ProductService productService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> createProduct(@ModelAttribute @Valid ProductCreateRequest request) {
        var result = productService.createProduct(request);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }
    
    @GetMapping("/all")
    public ApiResponse<List<ProductResponse>> getAllProducts() {
        var result = productService.getAllProducts();
        return ApiResponse.<List<ProductResponse>>builder().result(result).build();
    }
}
