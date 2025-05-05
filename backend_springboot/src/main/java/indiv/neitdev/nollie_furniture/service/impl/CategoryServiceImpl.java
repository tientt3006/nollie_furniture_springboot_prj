package indiv.neitdev.nollie_furniture.service.impl;

import indiv.neitdev.nollie_furniture.dto.request.CategoryCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryWithImageCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryWithImageUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.CategoryResponse;
import indiv.neitdev.nollie_furniture.entity.Category;
import indiv.neitdev.nollie_furniture.exception.AppException;
import indiv.neitdev.nollie_furniture.exception.ErrorCode;
import indiv.neitdev.nollie_furniture.mapper.CategoryMapper;
import indiv.neitdev.nollie_furniture.repository.CategoryRepository;
import indiv.neitdev.nollie_furniture.service.AwsS3Service;
import indiv.neitdev.nollie_furniture.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
    AwsS3Service awsS3Service;

    @Override
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        try {
            Category category = categoryMapper.toCategory(request);
            Category savedCategory = categoryRepository.save(category);
            return categoryMapper.toCategoryResponse(savedCategory);
        } catch (Exception e) {
//            log.error("Failed to create category: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CATEGORY_CREATE_FAILED);
        }
    }

    @Override
    public CategoryResponse createCategoryWithImage(CategoryWithImageCreateRequest request) {
        if (request.getImage() == null || request.getImage().isEmpty()) {
            throw new AppException(ErrorCode.CATEGORY_IMAGE_REQUIRED);
        }
        try {
            String imageUrl = awsS3Service.saveImageToS3(request.getImage());
            CategoryCreateRequest categoryCreateRequest = CategoryCreateRequest.builder()
                    .name(request.getName())
                    .imgUrl(imageUrl)
                    .build();
            return createCategory(categoryCreateRequest);
        } catch (AppException e) {
            throw e; // Rethrow AppExceptions directly
        } catch (Exception e) {
//            log.error("Failed to create category with image: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CATEGORY_CREATE_FAILED);
        }
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        try {
            return categoryRepository.findAll().stream()
                    .map(categoryMapper::toCategoryResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
//            log.error("Failed to get all categories: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.CATEGORY_FETCH_FAILED);
        }
    }

    @Override
    public CategoryResponse updateCategory(Integer id, CategoryUpdateRequest request) {
        if (Objects.isNull(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        
        try {
            Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            
            categoryMapper.updateCategory(category, request);
            Category updatedCategory = categoryRepository.save(category);
            return categoryMapper.toCategoryResponse(updatedCategory);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.CATEGORY_UPDATE_FAILED);
        }
    }
    
    @Override
    public CategoryResponse updateCategoryWithImage(Integer id, CategoryWithImageUpdateRequest request) {
        if (Objects.isNull(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (request.getImage() == null || request.getImage().isEmpty()) {
            throw new AppException(ErrorCode.CATEGORY_IMAGE_REQUIRED);
        }
        
        try {
            Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            
            String imageUrl = null;
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                // If there's an existing image URL and we're uploading a new image, delete the old one
                String oldImageUrl = category.getImgUrl();
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    // Extract filename from URL to delete
                    String filename = oldImageUrl.substring(oldImageUrl.lastIndexOf('/') + 1);
                    try {
                        awsS3Service.deleteImageFromS3(filename);
                    } catch (Exception e) {
                        // Log but continue - we still want to update even if deletion fails
                        log.warn("Failed to delete old image: {}", e.getMessage());
                    }
                }
                imageUrl = awsS3Service.saveImageToS3(request.getImage());
            }
            
            CategoryUpdateRequest categoryUpdateRequest = CategoryUpdateRequest.builder()
                    .name(request.getName())
                    .imgUrl(imageUrl)
                    .build();
                    
            categoryMapper.updateCategory(category, categoryUpdateRequest);
            Category updatedCategory = categoryRepository.save(category);
            return categoryMapper.toCategoryResponse(updatedCategory);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.CATEGORY_UPDATE_FAILED);
        }
    }
    
    @Override
    public void deleteCategory(Integer id) {
        if (Objects.isNull(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            
            Category category = categoryOpt.get();
            // Delete image from S3 if it exists
            String imageUrl = category.getImgUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                try {
                    awsS3Service.deleteImageFromS3(filename);
                } catch (Exception e) {
                    // Log but continue - we still want to delete the category even if image deletion fails
                    log.warn("Failed to delete category image: {}", e.getMessage());
                }
            }
            
            categoryRepository.deleteById(id);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.CATEGORY_DELETE_FAILED);
        }
    }

    @Override
    public CategoryResponse getCategory(Integer id) {
        return categoryMapper.toCategoryResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND)));
    }
}
