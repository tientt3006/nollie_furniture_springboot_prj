package indiv.neitdev.nollie_furniture.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductOptionAddRequest {
    int productId;
    int optionId;
}
