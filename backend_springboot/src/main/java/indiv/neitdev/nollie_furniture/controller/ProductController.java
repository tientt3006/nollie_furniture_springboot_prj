package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.*;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.response.CustomProductResponse;
import indiv.neitdev.nollie_furniture.dto.response.OptionResponse;
import indiv.neitdev.nollie_furniture.dto.response.ProductResponse;
import indiv.neitdev.nollie_furniture.entity.Product;
import indiv.neitdev.nollie_furniture.service.OrderService;
import indiv.neitdev.nollie_furniture.service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.internal.multipart.MpuRequestContext;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    ProductService productService;
    OrderService orderService;

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

    @GetMapping("/")
    public ApiResponse<CustomProductResponse> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        // Spring pageable bắt đầu từ 0
        PageRequest pageRequest = PageRequest.of(
                page - 1,
                size,
                Sort.by(sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy)
        );

        Page<Product> productPage = productService.getProducts(pageRequest, category, search);

        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(productService::toProductResponse) // Cần tạo code chuyển Product -> ProductResponse
                .toList();

        CustomProductResponse response = CustomProductResponse.builder()
                .currentPage(page)
                .pageSize(size)
                .totalItems(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .products(productResponses)
                .build();

        return ApiResponse.<CustomProductResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/top-sell/{number}")
    public ApiResponse<List<ProductResponse>> getTopSellingProducts(@PathVariable int number) {
        // Get top selling products from service
        List<Product> topSellingProducts = orderService.getTopSellingProducts(number);
        
        // Convert products to ProductResponse objects
        List<ProductResponse> productResponses = topSellingProducts.stream()
                .map(productService::toProductResponse)
                .collect(Collectors.toList());
                
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productResponses)
                .build();
    }
}
