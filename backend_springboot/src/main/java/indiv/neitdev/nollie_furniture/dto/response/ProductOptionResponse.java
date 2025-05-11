package indiv.neitdev.nollie_furniture.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductOptionResponse {
    int optionId;
    String optionName;
    int productOptionId;
    List<ProductOptionValueResponse> productOptionValueResponseList;
}
