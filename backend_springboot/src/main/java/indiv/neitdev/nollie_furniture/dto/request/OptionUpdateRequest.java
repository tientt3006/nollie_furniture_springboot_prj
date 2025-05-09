package indiv.neitdev.nollie_furniture.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionUpdateRequest {
    int id;
    String name;
    List<Integer> valueIdsForDelete;
    List<OptionValueCreateRequest> newValuesForAdd;
}
