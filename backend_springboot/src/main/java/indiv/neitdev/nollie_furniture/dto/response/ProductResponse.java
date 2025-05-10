package indiv.neitdev.nollie_furniture.dto.response;

import indiv.neitdev.nollie_furniture.entity.Category;
import indiv.neitdev.nollie_furniture.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    Integer productId;

    Category category;

    String name;

    BigDecimal basePrice;

    BigDecimal height;

    BigDecimal width;

    BigDecimal length;

    String description;
    
    Integer baseProductQuantity;

    String baseImageUrl;

    List<String> otherImagesUrl;

    List<ProductOptionResponse> productOptionResponseList;
}
