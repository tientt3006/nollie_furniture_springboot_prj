package indiv.neitdev.nollie_furniture.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddToCartReq {
    int productId;
    Integer baseProductQuantity; // nullable if productOptionValueIdsAndQuantity is not null
    List<Map<Integer, Integer>> productOptionValueIdsAndQuantity; // Nullable
}
