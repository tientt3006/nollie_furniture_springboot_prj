package indiv.neitdev.nollie_furniture.dto.request;

import indiv.neitdev.nollie_furniture.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateRequest {

    Category category;

    String name;

    BigDecimal basePrice;

    BigDecimal height;

    BigDecimal width;

    BigDecimal length;

    String description;
}
