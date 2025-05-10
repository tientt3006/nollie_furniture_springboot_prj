package indiv.neitdev.nollie_furniture.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductOptionValueResponse {
    int optionValueId;
    String optionValueName;
    String optionValueImgUrl;
    int productOptionValueId;
    Integer quantity;
    BigDecimal addPrice;
    String productOptionValueImgUrl;
}
