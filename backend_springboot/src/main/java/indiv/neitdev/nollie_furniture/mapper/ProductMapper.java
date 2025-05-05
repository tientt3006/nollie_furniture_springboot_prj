package indiv.neitdev.nollie_furniture.mapper;

import indiv.neitdev.nollie_furniture.dto.request.ProductCreateRequest;
import indiv.neitdev.nollie_furniture.dto.request.ProductUpdateRequest;
import indiv.neitdev.nollie_furniture.dto.response.ProductResponse;
import indiv.neitdev.nollie_furniture.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    Product toProduct(ProductCreateRequest product);

//    @Mapping(source = "", target = "", ignore = true)
    ProductResponse toProductResponse(Product product);

    void updateProduct(@MappingTarget Product product, ProductUpdateRequest request);
}
