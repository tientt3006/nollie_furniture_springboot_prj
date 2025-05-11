package indiv.neitdev.nollie_furniture.dto.response;

import indiv.neitdev.nollie_furniture.entity.Category;
import indiv.neitdev.nollie_furniture.entity.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    // Integer for id and String for imgUrl of entity ProductImg
    Map<Integer, String> baseImageUrl;

    List<Map<Integer, String>> otherImageUrl;

    List<ProductOptionResponse> productOptionResponseList;
}
