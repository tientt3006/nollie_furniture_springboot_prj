package indiv.neitdev.nollie_furniture.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    Integer id;
    Integer productId;
    String productName;
    Integer quantity;
    BigDecimal itemPrice;
    BigDecimal totalPrice; // itemPrice * quantity
    Integer productOptionValueId;
    String optionName;
    String optionValueName;
    String productImageUrl;
}
