package indiv.neitdev.nollie_furniture.dto.request;

import indiv.neitdev.nollie_furniture.entity.Category;
import indiv.neitdev.nollie_furniture.entity.Option;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateRequest {

    String name;

    BigDecimal basePrice;

    BigDecimal height;

    BigDecimal width;

    BigDecimal length;

    String description;

    Integer categoryId;

    MultipartFile baseProductImage;

    List<MultipartFile> otherProductImages;

    List<ProductOptionCreateRequest> productOptionCreateRequestList;


}
