package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.*;
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
    
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Integer id) {
        var result = productService.getProductById(id);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }
    
    @PostMapping("/update-base-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProductBaseInfo(@RequestBody @Valid ProdBaseInfoUpdateReq request) {
        var result = productService.updateProductBaseInfo(request);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }

    @PostMapping("/update-img")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProductImages(@ModelAttribute @Valid ProdImgUpdateReq request) {
        var result = productService.updateProductImages(request);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }

    @PostMapping("/update-opt-val")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProductOptionValue(@ModelAttribute @Valid ProdOptValUpdReq request) {
        var result = productService.updateProductOptionValue(request);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }

    @PostMapping("/add-opt-val")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> addProductOptionValue(@ModelAttribute @Valid ProdOptValAddReq request) {
        var result = productService.addProductOptionValue(request);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }

    @DeleteMapping("/delete-opt-val/{prodOptValId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> deleteProductOptionValue(@PathVariable Integer prodOptValId) {
        var result = productService.deleteProductOptionValue(prodOptValId);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }

    @PostMapping("/add-option")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> addProductOption(@RequestBody @Valid ProductOptionAddRequest request) {
        var result = productService.addProductOption(request);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }

    @DeleteMapping("/delete-opt/{prodOptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> deleteProductOption(@PathVariable Integer prodOptId) {
        var result = productService.deleteProductOption(prodOptId);
        return ApiResponse.<ProductResponse>builder().result(result).build();
    }

    @DeleteMapping("/delete/{prodId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteProduct(@PathVariable Integer prodId) {
        var result = productService.deleteProduct(prodId);
        return ApiResponse.<String>builder().result(result).build();
    }
}
