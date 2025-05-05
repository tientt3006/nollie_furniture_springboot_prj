package indiv.neitdev.nollie_furniture.service;

import indiv.neitdev.nollie_furniture.dto.request.CategoryCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryWithImageCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryWithImageUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryCreateRequest request);
    CategoryResponse createCategoryWithImage(CategoryWithImageCreateRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse updateCategory(Integer id, CategoryUpdateRequest request);
    CategoryResponse updateCategoryWithImage(Integer id, CategoryWithImageUpdateRequest request);
    void deleteCategory(Integer id);
    CategoryResponse getCategory(Integer id);
}
