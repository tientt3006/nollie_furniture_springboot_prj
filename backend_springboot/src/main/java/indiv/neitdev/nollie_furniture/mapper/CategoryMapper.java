package indiv.neitdev.nollie_furniture.mapper;

import indiv.neitdev.nollie_furniture.dto.request.CategoryCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.CategoryUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.CategoryResponse;
import indiv.neitdev.nollie_furniture.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {
    Category toCategory(CategoryCreateRequest category);

//    @Mapping(source = "", target = "", ignore = true)
    CategoryResponse toCategoryResponse(Category category);

    void updateCategory(@MappingTarget Category category, CategoryUpdateRequest request);
}
