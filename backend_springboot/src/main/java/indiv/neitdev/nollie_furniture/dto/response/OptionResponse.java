package indiv.neitdev.nollie_furniture.dto.response;

import indiv.neitdev.nollie_furniture.entity.OptionValue;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionResponse {
    int id;
    String name;
    List<OptionValue> values;
}
