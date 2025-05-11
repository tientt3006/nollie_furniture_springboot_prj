package indiv.neitdev.nollie_furniture.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProdBaseInfoUpdateReq {
    int productId;
    int categoryId;
    String name;
    BigDecimal basePrice;
    BigDecimal height;
    BigDecimal width;
    BigDecimal length;
    String description;
    int baseProductQuantity;
}
