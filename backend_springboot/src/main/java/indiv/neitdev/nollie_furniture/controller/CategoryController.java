package indiv.neitdev.nollie_furniture.controller;

import indiv.neitdev.nollie_furniture.dto.request.CategoryCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryWithImageCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryWithImageUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.ApiResponse;
import indiv.neitdev.nollie_furniture.dto.response.CategoryResponse;
import indiv.neitdev.nollie_furniture.repository.CategoryRepository;
import indiv.neitdev.nollie_furniture.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/category")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    CategoryService categoryService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryCreateRequest request) {
        var result = categoryService.createCategory(request);
        return ApiResponse.<CategoryResponse>builder().result(result).build();
    }

    @PostMapping(value = "create/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> createCategoryWithImage(@ModelAttribute @Valid CategoryWithImageCreateRequest request) {
        var result = categoryService.createCategoryWithImage(request);
        return ApiResponse.<CategoryResponse>builder().result(result).build();
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        var result = categoryService.getAllCategories();
        return ApiResponse.<List<CategoryResponse>>builder().result(result).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Integer id) {
        var result = categoryService.getCategory(id);
        return ApiResponse.<CategoryResponse>builder().result(result).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Integer id,
            @RequestBody @Valid CategoryUpdateRequest request) {
        var result = categoryService.updateCategory(id, request);
        return ApiResponse.<CategoryResponse>builder().result(result).build();
    }
    
    @PutMapping(value = "/with-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryResponse> updateCategoryWithImage(
            @PathVariable Integer id,
            @ModelAttribute @Valid CategoryWithImageUpdateRequest request) {
        var result = categoryService.updateCategoryWithImage(id, request);
        return ApiResponse.<CategoryResponse>builder().result(result).build();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }
}
